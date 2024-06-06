package neurord.numeric.grid;

import java.io.File;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import hdf.hdf5lib.H5;
import hdf.hdf5lib.HDF5Constants;

import neurord.util.ArrayUtil;
import static neurord.util.ArrayUtil.xJoined;
import neurord.util.Settings;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Level;

public class H5File {
    public static final Logger log = LogManager.getLogger();

    final static int compression_level = Settings.getProperty("neurord.compression",
                                                              "Compression level in HDF5 output",
                                                              1);

    static {
        if (System.getProperty("hdf.hdf5lib.H5.hdf5lib") == null) {
            if (new File("/usr/lib64/hdf5/libhdf5_java.so").exists()) {
                System.setProperty("hdf.hdf5lib.H5.hdf5lib", "/usr/lib64/hdf5/libhdf5_java.so");
            }
        }
    }

    long fd;
    Set<Group> groups;

    public H5File(File path)
        throws Exception
    {
        log.debug("Opening output file {}", path);

        try {
            this.fd = H5.H5Fcreate(path.toString(),
                                   HDF5Constants.H5F_ACC_TRUNC,
                                   HDF5Constants.H5P_DEFAULT,
                                   HDF5Constants.H5P_DEFAULT);
        } catch(Exception e) {
            log.error("Failed to open results file {}", path);
            throw e;
        }

        this.groups = new HashSet<>();

        assert(this.fd >= 0);
    }

    public H5File(String path)
        throws Exception
    {
        this(new File(path));
    }

    public void close()
        throws Exception
    {
        while (!this.groups.isEmpty())
            this.groups.iterator().next().close();

        log.debug("Closing H5File (fd={})", this.fd);

        if (this.fd >= 0) {
            H5.H5Fclose(this.fd);
            this.fd = HDF5Constants.H5I_INVALID_HID;
        }
    }

    public Group createGroup(String path)
        throws Exception
    {
        log.debug("Creating group {}", path);
        long id = H5.H5Gcreate(this.fd, path,
                               HDF5Constants.H5P_DEFAULT,
                               HDF5Constants.H5P_DEFAULT,
                               HDF5Constants.H5P_DEFAULT);
        return new Group(this, path, id);
    }

    protected Object _read(String path)
        throws Exception
    {
        long dset = H5.H5Dopen(this.fd, path, HDF5Constants.H5P_DEFAULT);

        try {
            final long dtype = H5.H5Dget_type(dset);
            final long dspace = H5.H5Dget_space(dset);

            try {
                final int ndims = H5.H5Sget_simple_extent_ndims(dspace);
                final long[] dims = new long[ndims];
                final long[] size = new long[ndims];

                H5.H5Sget_simple_extent_dims(dspace, dims, size);
                log.debug("{}: extents [{}], [{}]", path, xJoined(dims), xJoined(size));

                final int n = (int) ArrayUtil.product(dims);

                final Object data;
                if (H5.H5Tequal(dtype, HDF5Constants.H5T_STD_I32LE)) {
                    data = new int[n];
                    H5.H5Dread(dset,
                               HDF5Constants.H5T_NATIVE_INT,
                               HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT,
                               data);
                } else if (H5.H5Tequal(dtype, HDF5Constants.H5T_STD_I64LE)) {
                    data = new long[n];
                    H5.H5Dread(dset,
                               HDF5Constants.H5T_NATIVE_LONG,
                               HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT,
                               data);
                } else if (H5.H5Tequal(dtype, HDF5Constants.H5T_IEEE_F64LE)) {
                    data = new double[n];
                    H5.H5Dread(dset,
                               HDF5Constants.H5T_NATIVE_DOUBLE,
                               HDF5Constants.H5S_ALL, HDF5Constants.H5S_ALL, HDF5Constants.H5P_DEFAULT,
                               data);
                } else
                    throw new Exception("Uknown datatype " + dtype);

                return data;

            } finally {
                H5.H5Sclose(dspace);
                H5.H5Tclose(dtype);
            }
        } finally {
            H5.H5Dclose(dset);
        }
    }

    public <T> T read(String path)
        throws Exception
    {
        Object data = this._read(path);
        return (T) data;
    }

    public static class HObject {
        final String path;
        final long id;

        HObject(String path, long id) {
            assert path.charAt(0) == '/';
            this.path = path;
            this.id = id;
        }

        protected void setAttribute(String name, long type, Object value) {
            long dataspace_id = H5.H5Screate_simple(1, new long[]{1}, null);

            try {
                long attribute_id = H5.H5Acreate(this.id, name, type, dataspace_id,
                                                 HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

                try {
                    H5.H5Awrite(attribute_id, type, value);
                } finally {
                    H5.H5Aclose(attribute_id);
                }
            } finally {
                H5.H5Sclose(dataspace_id);
            }
        }

        public void setAttribute(String name, String value)
            throws Exception
        {
            byte[] bytes = value.getBytes("UTF-8");

            long type = H5.H5Tcreate(HDF5Constants.H5T_STRING, bytes.length);

            try {
                long status = H5.H5Tset_cset(type, HDF5Constants.H5T_CSET_UTF8);
                assert status >= 0;

                this.setAttribute(name, type, bytes);
            } finally {
                H5.H5Tclose(type);
            }

            log.debug("Wrote metadata on {} {}={}", this.path, name, value);
        }

        protected void setAttribute(String name, long value)
            throws Exception
        {
            this.setAttribute(name, HDF5Constants.H5T_STD_I64LE, new long[] {value});
            log.debug("Wrote metadata on {} {}={}", this.path, name, value);
        }

        protected long getAttribute(String name)
            throws Exception
        {
            long[] buf = new long[1];

            long attribute_id = H5.H5Aopen_by_name(this.id, ".", name,
                                                   HDF5Constants.H5P_DEFAULT, HDF5Constants.H5P_DEFAULT);

            try {
                H5.H5Aread(attribute_id, HDF5Constants.H5T_STD_I64LE, buf);
            } finally {
                H5.H5Aclose(attribute_id);
            }

            return buf[0];
        }
    }

    public static class Group extends HObject {
        final H5File file;
        Set<Dataset> children;

        protected Group(H5File file, String path, long id)
            throws Exception
        {
            super(path, id);
            this.file = file;
            file.groups.add(this);
            this.children = new HashSet<>();
        }

        public Group createSubGroup(String name)
            throws Exception
        {
            return this.file.createGroup(this.path + "/" + name);
        }

        public void close() {
            while (!this.children.isEmpty())
                this.children.iterator().next().close();

            log.debug("Closing Group {} (id={})", this.path, this.id);
            this.file.groups.remove(this);
            H5.H5Gclose(this.id);
        }

        protected long[] makeChunks(long dims[], boolean extensible) {
            long[] chunks = dims.clone();

            if (!extensible)
                return null; /* We don't do compression for non-extensible arrays.
                              * It turns out to be inefficient because we have a bunch of
                              * very small items. */

            /* Avoid too small chunks */
            for (chunks[0] = 1;; chunks[0] *= 2) {
                long size = ArrayUtil.product(chunks);

                if (size == 0)
                    return null;
                if (size > 1024) /* This will be multiplied by the field size, usually 4 or 8 bytes */
                    return chunks;
            }
        }

        protected Dataset _createArray(String name,
                                       long type,
                                       long[] dims,
                                       Object data,
                                       boolean deflate,
                                       boolean extensible)
            throws Exception
        {
            String path = this.path + "/" + name;

            final long[] size;
            if (extensible) {
                size = dims.clone();
                size[0] = HDF5Constants.H5F_UNLIMITED;
            } else
                size = null;

            /* chunking and/or compression are broken for empty arrays */
            long[] chunks = makeChunks(dims, extensible);

            log.debug("Creating {} with dims=[{}] size=[{}] chunks=[{}]...",
                      path,
                      xJoined(dims),
                      size != null ? xJoined(size) : "",
                      chunks != null ? xJoined(chunks) : "");

            long dataspace_id = H5.H5Screate_simple(dims.length, dims, size);
            final long id;

            try {
                /* Create the dataset creation property list, add the shuffle filter and the gzip
                 * compression filter. The order in which the filters are added here is significant
                 * — we will see much greater results when the shuffle is applied first. The order
                 * in which the filters are added to the property list is the order in which they
                 * will be invoked when writing data. */
                long dcpl_id = H5.H5Pcreate(HDF5Constants.H5P_DATASET_CREATE);

                try {
                    if (deflate) {
                        H5.H5Pset_shuffle(dcpl_id);
                        H5.H5Pset_deflate(dcpl_id, compression_level);
                    }

                    if (chunks != null)
                        H5.H5Pset_chunk(dcpl_id, dims.length, chunks);

                    id = H5.H5Dcreate(this.file.fd, path,
                                      type,
                                      dataspace_id,
                                      HDF5Constants.H5P_DEFAULT,
                                      dcpl_id,
                                      HDF5Constants.H5P_DEFAULT);
                } finally {
                    H5.H5Pclose(dcpl_id);
                }
            } finally {
                H5.H5Sclose(dataspace_id);
            }

            if (data != null)
                H5.H5Dwrite(id, type,
                            HDF5Constants.H5S_ALL,
                            HDF5Constants.H5S_ALL,
                            HDF5Constants.H5P_DEFAULT,
                            data);

            log.info("Created {} with dims=[{}] size=[{}] chunks=[{}]",
                     path,
                     xJoined(dims),
                     size != null ? xJoined(size) : "",
                     chunks != null ? xJoined(chunks) : "");

            return new Dataset(this, path, id, type, dims, chunks);
        }

        protected Dataset _createArray(String name,
                                      long type,
                                      long[] dims,
                                      Object data)
            throws Exception
        {
            return this._createArray(name, type, dims, data, false, false);
        }

        public Dataset writeVector(String name, String... items)
            throws Exception
        {
            int maxlength = Math.max(ArrayUtil.maxLength(items) * 4, 1) + 1;

            byte[][] bytes = new byte[items.length][];
            for (int i = 0; i < bytes.length; i++) {
                bytes[i] = new byte[maxlength];
                byte[] c = items[i].getBytes("UTF-8");
                System.arraycopy(c, 0, bytes[i], 0, c.length);
            }

            long[] dims = {items.length};

            long type = H5.H5Tcreate(HDF5Constants.H5T_STRING, maxlength);

            final Dataset ds;
            try {
                long status = H5.H5Tset_cset(type, HDF5Constants.H5T_CSET_UTF8);
                assert status >= 0;

                ds = this._createArray(name, type, dims, bytes);
            } finally {
                H5.H5Tclose(type);
            }

            return ds;
        }

        protected Dataset writeVector(String name, double... items)
            throws Exception
        {
            long[] dims = {items.length};
            return this._createArray(name, HDF5Constants.H5T_IEEE_F64LE, dims, items);
        }

        protected Dataset writeVector(String name, int... items)
            throws Exception
        {
            long[] dims = {items.length};
            return this._createArray(name, HDF5Constants.H5T_INTEL_I32, dims, items);
        }

        protected Dataset writeVector(String name, long... items)
            throws Exception
        {
            long[] dims = {items.length};
            return this._createArray(name, HDF5Constants.H5T_INTEL_I64, dims, items);
        }

        protected Dataset writeArray(String name, double[][] items)
            throws Exception
        {
            int maxlength = ArrayUtil.maxLength(items);
            double[] flat = ArrayUtil.flatten(items, maxlength);

            long[] dims = {items.length, maxlength};
            return this._createArray(name, HDF5Constants.H5T_IEEE_F64LE, dims, flat);
        }

        protected Dataset writeArray(String name, int[][] items, int fill)
            throws Exception
        {
            int maxlength = ArrayUtil.maxLength(items);
            int[] flat = ArrayUtil.flatten(items, maxlength, fill);

            long[] dims = {items.length, maxlength};
            return this._createArray(name, HDF5Constants.H5T_INTEL_I32, dims, flat);
        }

        public void writeMap(Set<Map.Entry<Object,Object>> set)
            throws Exception
        {
            for (Map.Entry<Object,Object> entry: set) {
                /* Manifest keys allow ascii characters, numbers, dashes and underscores. */
                String key = entry.getKey().toString();
                String value = (String) entry.getValue();

                this.setAttribute(key, value);
            }
        }

        protected Dataset createExtensibleArray(String name, long type,
                                                String TITLE, String LAYOUT, String UNITS,
                                                long... dims)
            throws Exception
        {
            long[] dims0 = dims.clone();
            dims0[0] = 0;

            Dataset ds = this._createArray(name, type, dims0, null, true, true);

            ds.setAttribute("TITLE", TITLE);
            ds.setAttribute("LAYOUT", LAYOUT);
            ds.setAttribute("UNITS", UNITS);

            return ds;
        }

        protected long typeId(Class type, Long string_type)
            throws Exception
        {
            if (type == int.class)
                return HDF5Constants.H5T_STD_I32LE;
            else if (type == long.class)
                return HDF5Constants.H5T_STD_I64LE;
            else if (type == double.class)
                return HDF5Constants.H5T_IEEE_F64LE;
            else if (type == String.class && string_type != null)
                return string_type;
            else
                throw new Exception("Uknown type " + type);
        }

        protected Dataset createExtensibleArray(String name, Class type,
                                                String TITLE, String LAYOUT, String UNITS,
                                                long... dims)
            throws Exception
        {
            final long type_id = typeId(type, null);

            return createExtensibleArray(name, type_id,
                                         TITLE, LAYOUT, UNITS,
                                         dims);
        }

        protected int compound_type_size(Class... memberTypes) {
            int size = 0;

            for (Class type: memberTypes)
                if (type == double.class)
                    size += 8;
                else if (type == int.class)
                    size += 4;
                else if (type == String.class)
                    size += 100;
                else
                    throw new RuntimeException("unknown type");

            return size;
        }

        protected Dataset writeCompoundDS(String name,
                                          String[] memberNames, Class[] memberTypes,
                                          int count, Object[] data)
            throws Exception
        {
            final String path = this.path + "/" + name;
            final long[] dims = new long[]{ count };

            long dataspace_id = H5.H5Screate_simple(dims.length, dims, null);
            final long id;

            final int type_size = compound_type_size(memberTypes);
            final long type;

            try {
                long dcpl_id = H5.H5Pcreate(HDF5Constants.H5P_DATASET_CREATE);

                try {
                    if (compression_level > 0) {
                        H5.H5Pset_shuffle(dcpl_id);
                        H5.H5Pset_deflate(dcpl_id, compression_level);
                        H5.H5Pset_chunk(dcpl_id, dims.length, dims);
                    }

                    long strtype = H5.H5Tcopy(HDF5Constants.H5T_C_S1);
                    H5.H5Tset_size(strtype, 100);

                    type = H5.H5Tcreate(HDF5Constants.H5T_COMPOUND, type_size);

                    long offset = 0;
                    for (int k = 0; k < memberNames.length; k++) {
                        long member_type = typeId(memberTypes[k], strtype);
                        H5.H5Tinsert(type, memberNames[k], offset, member_type);
                        offset += compound_type_size(memberTypes[k]);
                    }
                    assert offset == type_size;

                    id = H5.H5Dcreate(this.file.fd, path,
                                      type,
                                      dataspace_id,
                                      HDF5Constants.H5P_DEFAULT,
                                      dcpl_id,
                                      HDF5Constants.H5P_DEFAULT);

                    H5.H5Tclose(strtype);

                } finally {
                    H5.H5Pclose(dcpl_id);
                }
            } finally {
                H5.H5Sclose(dataspace_id);
            }

            byte[] bytes = new byte[count * type_size];
            ByteBuffer bytes_buf = ByteBuffer.wrap(bytes);
            bytes_buf.order(ByteOrder.nativeOrder());

            for (int i = 0; i < dims[0]; i++)
                for (int k = 0; k < memberTypes.length; k++)
                    if (memberTypes[k] == double.class)
                        bytes_buf.putDouble(((double[]) data[k])[i]);
                    else if (memberTypes[k] == int.class)
                        bytes_buf.putInt(((int[]) data[k])[i]);
                    else if (memberTypes[k] == String.class) {
                        byte[] t = ((String[]) data[k])[i].getBytes();
                        bytes_buf.put(t);
                        bytes_buf.position(bytes_buf.position() + 100 - t.length);
                    } else
                        throw new RuntimeException("unknown type");

            H5.H5Dwrite(id, type,
                        HDF5Constants.H5S_ALL,
                        HDF5Constants.H5S_ALL,
                        HDF5Constants.H5P_DEFAULT,
                        bytes);

            return new Dataset(this, path, id, type, dims, null);
        }
    }

    public static class Dataset extends HObject {
        final Group group;
        final long[] dimensions, chunks;
        final long type;

        public Dataset(Group group, String path, long id, long type, long[] dimensions, long[] chunks)
            throws Exception
        {
            super(path, id);
            this.group = group;
            group.children.add(this);

            this.dimensions = dimensions;
            this.chunks = chunks;
            this.type = type;
        }

        public void close() {
            log.debug("Closing Dataset {} (id={})", this.path, this.id);
            this.group.children.remove(this);

            H5.H5Dclose(this.id);
        }

        protected void extend(long howmuch, Object data)
            throws Exception
        {
            long[] total_size = this.dimensions.clone();
            total_size[0] += howmuch;

            boolean good = false;

            try {
                H5.H5Dset_extent(this.id, total_size);

                /* Select a hyperslab in extended portion of dataset  */
                long[] size_new = this.dimensions.clone();
                size_new[0] = howmuch;

                long[] offsets = new long[this.dimensions.length];
                offsets[0] = this.dimensions[0];

                long filespace = H5.H5Dget_space(this.id);

                try {
                    H5.H5Sselect_hyperslab(filespace, HDF5Constants.H5S_SELECT_SET, offsets, null, size_new, null);

                    /* Define memory space */
                    long memspace = H5.H5Screate_simple(size_new.length, size_new, null);

                    try {
                        /* Write the data to the extended portion of dataset  */
                        H5.H5Dwrite(this.id, this.type, memspace, filespace, HDF5Constants.H5P_DEFAULT, data);
                        good = true;

                        this.dimensions[0] += howmuch;
                    } finally {
                        H5.H5Sclose(memspace);
                    }

                } finally {
                    H5.H5Sclose(filespace);
                };
            } finally {
                if (!good)

                    log.error("Write failed dimensions={} howmuch={}",
                              xJoined(this.dimensions), howmuch);
            }
        }
    }
}
