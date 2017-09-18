package org.aioobe.cloudconvert;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

import java.io.*;
import java.net.URI;
import java.text.ParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.client.*;
import javax.ws.rs.core.MediaType;

import org.glassfish.jersey.media.multipart.*;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition.FormDataContentDispositionBuilder;
import org.glassfish.jersey.media.multipart.file.*;

public class ConvertProcess {
    
    WebTarget root;
    ProcessArguments args;
    
    /**
     * Creates a {@code ConvertProcess} for the given URI.
     */
    public ConvertProcess(URI root) {
        this(root, null);
    }
    
    /**
     * Creates a {@code ConvertProcess} for the given URI and process arguments.
     */
    public ConvertProcess(URI root, ProcessArguments args) {
        this.root = ClientUtil.createClient().target(root);
        this.args = args;
    }
    
    /**
     * Retrieves the current status of the conversion process.
     */
    public ProcessStatus getStatus() {
        return root.request(APPLICATION_JSON)
                   .get(ProcessStatus.class);
    }

    /**
     * Starts the conversion process for a given file.
     */
    public void startConversion(File file, Map<String,String> options) throws ParseException, FileNotFoundException {
        if (!file.exists())
            throw new FileNotFoundException("File not found: " + file);
        startConversion(new FileDataBodyPart("file", file), options);
    }

    /**
     * Starts the conversion process for a given file.
     */
    public void startConversion(File file) throws ParseException, FileNotFoundException {
        startConversion(file, new HashMap<String, String>());
    }


    /**
     * Starts the conversion process for a given {@code InputStream} and file name.
     */
    public void startConversion(InputStream input, String filename, Map<String,String> options) throws ParseException {
        StreamDataBodyPart filePart = new StreamDataBodyPart("file", input);
        FormDataContentDispositionBuilder builder = FormDataContentDisposition.name("file")
                .fileName(filename);
        filePart.setFormDataContentDisposition(builder.build());
        startConversion(filePart, options);
    }

    /**
     * Starts the conversion process for a given {@code InputStream} and file name.
     */
    public void startConversion(InputStream input, String filename) throws ParseException {
        startConversion(input, filename, new HashMap<String, String>());
    }
    
    /**
     * Starts the conversion process for a given {@code BodyPart}.
     */
    private void startConversion(BodyPart bodyPart, Map<String,String> options) {
        
        if (args == null)
            throw new IllegalStateException("No conversion arguments set.");

        FormDataMultiPart builder = new FormDataMultiPart().field("input", "upload")
                .field("outputformat", args.outputformat);

        for (Map.Entry<String, String> option : options.entrySet()) {
            builder.field(option.getKey(), option.getValue());
        }

        MultiPart multipart = builder.bodyPart(bodyPart);

        root.request(MediaType.APPLICATION_JSON)
            .post(Entity.entity(multipart, multipart.getMediaType()));
    }
    
    /**
     * Deletes the conversion process.
     */
    public void delete() {
        root.path("delete").request().get();
    }
    
    /**
     * Cancels the conversion process.
     */
    public void cancel() {
        root.path("cancel").request().get();
    }
    
}
