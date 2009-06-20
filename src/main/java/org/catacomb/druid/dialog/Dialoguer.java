package org.catacomb.druid.dialog;


import java.io.File;

import org.catacomb.druid.util.FileChooser;
import org.catacomb.interlish.content.StringValue;
import org.catacomb.interlish.report.Logger;



public class Dialoguer {


    static CheckSeenDialogController csdController;
    static NamingDialogController namingDialogController;
    static MessageDialogController messageDialogController;
    static ConfirmationDialogController confirmationDialogController;
    static QuestionDialogController questionDialogController;
    static TextDialogController textDialogController;

    static FolderDialogController folderDialogController;

    static ProgressLogDialogController progressLogController;




    public static void checkSeen(String label, String msg, int[] xy) {
        if (csdController == null) {
            csdController = new CheckSeenDialogController();
        }

        csdController.showIfNotYetSeen(label, msg, xy);
    }



    public static String getNewName(int[] xy, String msg) {
        return getNewName(xy, msg, "");
    }


    public static String getNewName(int[] xy, String msg, String initValue) {
        String ret = null;
        if (namingDialogController == null) {
            namingDialogController = new NamingDialogController();
        }
        ret = namingDialogController.getNewName(xy, msg, initValue);
        return ret;
    }


    public static File getFolder(int[] xy, String msg, File fdef) {
        File ret = null;
        if (folderDialogController == null) {
            folderDialogController = new FolderDialogController();
        }
        ret = folderDialogController.getFolder(xy, msg, fdef);
        return ret;
    }

    public static File getFile(String mode) {
        return FileChooser.getChooser().getFileToOpen(mode);
    }


    public static File getFileToWrite(String mode, File fdir, String ext, String extDef) {
        FileChooser.getChooser().setDefaultFolderForMode(mode, fdir);
        return FileChooser.getChooser().getFileToWrite(mode, ext, extDef);
    }

    public static File getFileToWrite(String mode) {
        return FileChooser.getChooser().getFileToWrite(mode);
    }


    public static File getFileToRead(String mode) {
        return getFile(mode);
    }



    public static boolean getConfirmation(int[] xy, String msg) {
        boolean ret = false;
        if (confirmationDialogController == null) {
            confirmationDialogController = new ConfirmationDialogController();
        }
        ret = confirmationDialogController.getResponse(xy, msg);
        return ret;
    }

    public static void message(String msg) {
        int[] ixy = {400, 300};
        message(ixy, msg);
    }


    public static void message(int[] xy, String msg) {
        message(xy, "", msg);
    }

    public static void message(int[] xy, String title, String msg) {
        if (messageDialogController == null) {
            messageDialogController = new MessageDialogController();

        }
        messageDialogController.show(xy, title, msg);
    }



    public static int multiChoiceLongQuestion(String ques, String[] answers) {
        if (questionDialogController == null) {
            questionDialogController = new QuestionDialogController();
        }
        int ret = questionDialogController.getResponse(ques, answers);
        return ret;
    }


    public static void showText(String s) {
        int[] ixy = {400, 300};
        StringValue txtsv = new StringValue(s);
        showText(ixy, txtsv);
    }

    public static void showText(int[] xy, StringValue txtsv) {
        if (textDialogController == null) {
            textDialogController = new TextDialogController();

        }
        textDialogController.showNonModal(xy, txtsv);
    }



    public static Logger getProgressLogger() {
        if (progressLogController == null) {
            progressLogController = new ProgressLogDialogController();
            progressLogController.checkInit();
        }
        return progressLogController;
    }



    public static void closeProgressLogger() {
        progressLogController.close();

    }



}
