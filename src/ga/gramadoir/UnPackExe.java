/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package ga.gramadoir;

import java.io.File;
import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;





/**
 *
 * @author root
 */
public class UnPackExe extends Main {

    private boolean unpacked=false;

    private String[] hashFiles= {"earraidi.hash","nocombo.hash","pos.hash", "eile.hash", "3grams.hash","messages.hash","focail0.hash","focail1.hash","focail2.hash",
                                "Languages.pm","focail3.hash","focail4.hash","focail5.hash","focail6.hash","token.hash","morph.hash"};
    
    private static String exe = "gram.exe";
    private static String installDir;
    private String separator;

   public void unpack(String installDir)
        throws URISyntaxException,
               ZipException,
               IOException
    {
        final URI uri;
        this.installDir=installDir;
        separator=File.separator;
        uri = getJarURI();
        String libDir=installDir+"Lingua"+separator+"GA"+separator+"Gramadoir"+separator;
        //File dir = new File(uri.getRawPath());




        for(String file: hashFiles)
           unpackFile(uri, file, libDir);

        unpackFile(uri, exe, installDir);

        }



    private static URI getJarURI()
        throws URISyntaxException
    {
        final ProtectionDomain domain;
        final CodeSource       source;
        final URL              url;
        final URI              uri;

        domain = Main.class.getProtectionDomain();

        source = domain.getCodeSource();
        url    = source.getLocation();
        uri    = url.toURI();


        return (uri);
    }

    private String unpackFile(final URI    where,
                               final String fileName, final String dir)
        throws ZipException,
               IOException
    {
        final File location;
        final URI  fileURI;

        location = new File(where);

        // not in a JAR, just return the path on disk
        if(location.isDirectory())
        {
            fileURI = URI.create(where.toString() + fileName);
        }
        else
        {
            final ZipFile zipFile;

            zipFile = new ZipFile(location);
            //zipFile=new ZipFile("/home/ciaran/NetBeansProjects/")

            try
            {
                fileURI = extract2(zipFile, fileName, dir);
            }
            finally
            {
                zipFile.close();
            }
        }

        return (fileURI.toString());
    }


     private URI extract2(final ZipFile zipFile,
                               final String  fileName, String dir)
        throws IOException
    {
        final File         tempFile;
        final ZipEntry     entry;
        final InputStream  zipStream;
        OutputStream       fileStream;

        File libDir = new File(dir);
        if (!libDir.exists()){
          libDir.mkdirs();
        }
        if(fileName.equalsIgnoreCase("gram.exe")){
            tempFile=new File(installDir+fileName);
            tempFile.createNewFile();
            tempFile.setExecutable(true);
        }
        else{
            String file=libDir+separator+fileName;
            tempFile =  new File(file);
            tempFile.createNewFile();
        }
        
        entry = zipFile.getEntry(fileName);

        if(entry == null)
        {
            throw new FileNotFoundException("cannot find file: " + fileName + " in archive: " + zipFile.getName());
        }

        zipStream  = zipFile.getInputStream(entry);
        fileStream = null;

        try
        {
            final byte[] buf;
            int          i;

            fileStream = new FileOutputStream(tempFile);
            buf        = new byte[1024];
            i          = 0;

            while((i = zipStream.read(buf)) != -1)
            {
                fileStream.write(buf, 0, i);
            }
        }
        finally
        {
            close(zipStream);
            close(fileStream);
        }

        return (tempFile.toURI());
    }

    private static void close(final Closeable stream)
    {
        if(stream != null)
        {
            try
            {
                stream.close();
            }
            catch(final IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

}