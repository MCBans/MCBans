package com.mcbans.firestar.mcbans.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;

import com.mcbans.firestar.mcbans.ActionLog;
import com.mcbans.firestar.mcbans.MCBans;


public class FileStructure {
    /**
     * Create directory
     * @param dir
     */
    public static void createDir(final File dir){
        // if already exists, do nothing
        if (dir.isDirectory()){
            return;
        }
        if (!dir.mkdir()){
            ActionLog.getInstance().warning("Cannot create directory: " + dir.getName());
        }
    }

    /**
     * Copy file from [srcPath] to [destPath]
     * Use FileChannel#transferTo
     * @param srcPath From Path
     * @param destPath  To Path
     * @throws IOException IOException
     */
    @SuppressWarnings("resource")
	public static void copyTransfer(String srcPath, String destPath) throws IOException {
        FileChannel srcChannel = null, destChannel = null;
        try {
            srcChannel = new FileInputStream(srcPath).getChannel();
            destChannel = new FileOutputStream(destPath).getChannel();

            srcChannel.transferTo(0, srcChannel.size(), destChannel);
        } finally {
            srcChannel.close();
            destChannel.close();
        }
    }

    /**
     * Extract resource file
     * @param from From path
     * @param to To path
     * @param force if true, override exist file
     * @param lang if true, extract with another method
     */
    public static void extractResource(String from, File to, boolean force, boolean lang){
        File of = to;

        // if to path is directory, cast to File. return if not file or directory
        if (to.isDirectory()){
            String filename = new File(from).getName();
            of = new File(to, filename);
        }else if(!of.isFile()){
            ActionLog.getInstance().warning("Not a file:" + of);
            return;
        }

        // if file exist, check force flag
        if (of.exists() && !force){
            return;
        }

        OutputStream out = null;
        InputStream in = null;
        InputStreamReader reader = null;
        OutputStreamWriter writer =null;
        try{
            // get inside jar resource uri
            URL res = MCBans.class.getResource(from);
            if (res == null){
                ActionLog.getInstance().warning("Can't find "+ from +" in plugin jar file");
                return;
            }
            URLConnection resConn = res.openConnection();
            resConn.setUseCaches(false);
            in = resConn.getInputStream();

            if (in == null){
                ActionLog.getInstance().warning("Can't get input stream from " + res);
            }else{
                // write file
                if (lang){
                    reader = new InputStreamReader(in, "UTF-8");
                    writer = new OutputStreamWriter(new FileOutputStream(of)); // not specify output encode

                    int text;
                    while ((text = reader.read()) != -1){
                        writer.write(text);
                    }
                }else{
                    out = new FileOutputStream(of);
                    byte[] buf = new byte[1024]; // Buffer size
                    int len = 0;
                    while((len = in.read(buf)) >= 0){
                        out.write(buf, 0, len);
                    }
                }
            }
        }catch (Exception ex){
            ex.printStackTrace();
        }finally{
            // close stream
            try{ if (out != null) out.close(); } catch (Exception ignore){}
            try{ if (in != null) in.close(); } catch (Exception ignore){}
            try{ if (reader != null) reader.close(); } catch (Exception ignore){}
            try{ if (writer != null) writer.close(); } catch (Exception ignore){}
        }
    }

    public static File getPluginDir(){
        return MCBans.getInstance().getDataFolder();
    }
}
