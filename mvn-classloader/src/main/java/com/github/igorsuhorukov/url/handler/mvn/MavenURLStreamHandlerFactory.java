package com.github.igorsuhorukov.url.handler.mvn;

import com.github.smreed.dropship.ClassLoaderBuilder;
import com.github.smreed.dropship.MavenClassLoader;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.util.logging.Logger;

/**
 * Support URL syntax for maven repository artifacts.
 *
 * For example mvn:/com.github.igor-suhorukov:aspectj-scripting:pom:1.3?https://jcenter.bintray.com
 */
public class MavenURLStreamHandlerFactory implements java.net.URLStreamHandlerFactory{

    final static private Logger logger = Logger.getLogger(MavenURLStreamHandlerFactory.class.getName());

    public static final String MVN_PROTOCOL = "mvn";

    protected java.net.URLStreamHandlerFactory urlStreamHandlerFactory;

    public MavenURLStreamHandlerFactory() {
    }

    public MavenURLStreamHandlerFactory(java.net.URLStreamHandlerFactory urlStreamHandlerFactory) {
        this.urlStreamHandlerFactory = urlStreamHandlerFactory;
    }

    @Override
    public URLStreamHandler createURLStreamHandler(String protocol) {
        if (MVN_PROTOCOL.equals(protocol)){ return new URLStreamHandler() {
            protected URLConnection openConnection(URL url) throws IOException {
                try {
                    String repository = url.getQuery();
                    ClassLoaderBuilder classLoaderBuilder;
                    if (repository == null){
                        classLoaderBuilder = MavenClassLoader.usingCentralRepo();
                    } else {
                        classLoaderBuilder = MavenClassLoader.using(repository);
                    }
                    return classLoaderBuilder.resolveArtifact(getUrlPath(url)).openConnection();
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
        };} else{
            if(urlStreamHandlerFactory !=null){
                try {
                    return urlStreamHandlerFactory.createURLStreamHandler(protocol);
                } catch (Exception ignore) {
                    logger.warning(ignore.getMessage());
                    return null;
                }
            } else {
                return null;
            }
        }
    }

    private static String getUrlPath(URL url) {
        if(StringUtils.hasText(url.getPath()) && url.getPath().startsWith("/")){
            return url.getPath().substring(1);
        } else {
            return url.getPath();
        }
    }
}
