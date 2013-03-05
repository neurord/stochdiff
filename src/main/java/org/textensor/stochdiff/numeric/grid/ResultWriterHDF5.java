package org.textensor.stochdiff.numeric.grid;

import java.io.File;
import java.util.Arrays;
import java.util.Vector;
import javax.swing.tree.DefaultMutableTreeNode;

import static ncsa.hdf.hdf5lib.HDF5Constants.H5F_UNLIMITED;
import ncsa.hdf.object.Datatype;
import ncsa.hdf.object.Dataset;
import ncsa.hdf.object.Group;
import ncsa.hdf.object.FileFormat;
import ncsa.hdf.object.h5.H5File;
import ncsa.hdf.object.h5.H5Datatype;

import org.textensor.stochdiff.numeric.morph.VolumeGrid;
import org.textensor.util.FileUtil;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

public class ResultWriterHDF5 implements ResultWriter {
    static final Logger log = LogManager.getLogger(ResultWriterHDF5.class);

    final protected File outputFile;
    protected H5File output;
    protected Group sim;

    public ResultWriterHDF5(File outFile) {
        this.outputFile = new File(FileUtil.getRootName(outFile) + ".h5");
        log.info("Writing HDF5 to {}", this.outputFile);
    }

    @Override
    public void init(String magic) {
        FileFormat fileFormat = FileFormat.getFileFormat(FileFormat.FILE_TYPE_HDF5);
        assert fileFormat != null;
        log.info("Opening output file {}", this.outputFile);
        try {
            this.output = (H5File) fileFormat.create(this.outputFile.toString());
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        assert this.output != null;
        try {
            this.output.open();
        } catch(Exception e) {
            log.error("Failed to open results file {}", this.outputFile);
            throw new RuntimeException(e);
        }

        Group root = (Group)((DefaultMutableTreeNode) this.output.getRootNode()).getUserObject();
        try {
            this.sim = this.output.createGroup("simulation", root);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() {
        log.info("Closing output file {}", this.outputFile);
        try {
            this.output.close();
        } catch(Exception e) {
            log.error("Failed to close results file {}", outputFile, e);
        }
    }

    @Override
    public void writeGrid(VolumeGrid vgrid, double startTime, String fnmsOut[], IGridCalc source) {
        int n = vgrid.getNElements();
        long[]
            dims = {n,},
            chunks = {n,};
        int gzip = 6;
        String[] memberNames = {"x0", "y0", "z0",
                                "x1", "y1", "z1",
                                "x2", "y2", "z2",
                                "x3", "y3", "z3",
                                "volume", "deltaZ"};

        Datatype[] memberTypes = new Datatype[memberNames.length];
        Arrays.fill(memberTypes,
                    new H5Datatype(Datatype.CLASS_FLOAT, 8, Datatype.NATIVE, Datatype.NATIVE));

        Vector<Object> data = vgrid.gridData();

        Dataset grid;
        try {
            grid = this.output.createCompoundDS("grid", this.sim, dims, null, chunks, gzip,
                                                memberNames, memberTypes, null, data);
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void writeGridConcs(double time, int nel, int ispecout[], IGridCalc source) {}

    @Override
    public void writeGridConcsDumb(int i, double time, int nel, String fnamepart, IGridCalc source) {}

    @Override
    public void saveState(double time, String prefix, String state) {}
}
