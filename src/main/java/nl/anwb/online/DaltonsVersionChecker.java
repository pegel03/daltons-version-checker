package nl.anwb.online;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;

public class DaltonsVersionChecker {

    private static final String ENVIRONMENT = "-environment";
    private static final String SEPARATOR = "\t";
    private static final String LINE_FEED = "\n";
    private static final String APP = "app";
    private static final String ERR = "ERR";

    // http://www-environment.anwb.nl/pois/monitor
    // https://www-environment.anwb.nl/redactieuitjes/monitor,peterderijcke,puNiebi6
    // http://webservices-environment.anwb.nl/poi-mongoimport-satellite/monitor
    // https://www-environment.anwb.nl/mijn-anwb/mijn-wegenwacht/monitor
    // https://cms-environment.anwb.nl/ebike-beheer/monitor,pglas,EJBB6nI89oOY
    //

    private static List<String> rows = new ArrayList<String>();
    // "-test2",
    private static String[] envs = { "-ontw", "-test", "-acc", "" };
    private static String[] urlDatas;

    public final static void main(String[] args) {
        // System.setProperty("javax.net.debug", "all");
        try {
            readUrlDataFromConfigFile();
            initFirstRow();
            buildReport();
            printReport();
        } catch (DaltonRuntimeException e) {
            System.out.println("catch all: ");
            e.printStackTrace();
        }
    }

    private static void readUrlDataFromConfigFile() throws DaltonRuntimeException {
        InputStream stream = null;
        try {
            stream = new FileInputStream("." + File.separator + "versionCheckerConfig.txt");

            String resource = IOUtils.toString(stream);
            System.out.println("Resources:");
            System.out.println(resource);
            System.out.println("####");
            urlDatas = StringUtils.split(resource);
        } catch (IOException e) {
            throw new DaltonRuntimeException("While reading config", e);
        } finally {
            IOUtils.closeQuietly(stream);
        }
    }

    private static void initFirstRow() {
        StringBuilder row = new StringBuilder();
        row.append("env").append(SEPARATOR);
        for (String env : envs) {
            row.append(env).append(SEPARATOR);
        }
        rows.add(row.toString());
    }

    private static void buildReport() throws DaltonRuntimeException {
        for (String url : urlDatas) {
            List<String> rowCols = new ArrayList<String>();
            rowCols.add(APP);
            for (String env : envs) {
                String response = null;
                try {
                    String realUrl = StringUtils.replace(url, ENVIRONMENT, env);
                    response = getDataFromUrl(realUrl);
                } catch (DaltonInvalidResponseException e) {
                    response = ERR;
                }
                processAppInfo(response, rowCols);
            }
            rows.add(StringUtils.join(rowCols, SEPARATOR));
        }
    }

    private static String getDataFromUrl(String url) throws DaltonRuntimeException, DaltonInvalidResponseException {
        HttpURLConnection uc = null;
        InputStream stream = null;
        String response;
        String[] split = StringUtils.split(url, ',');
        try {
            uc = (HttpURLConnection) openConnectionForUrl(split[0]);
            if (split.length > 1) {
                String basicAuth = constructAuthHeader(split[1], split[2]);
                uc.setRequestProperty("Authorization", basicAuth);
            }
            String contentType = uc.getContentType();
            // Object content = uc.getContent();
            int code = uc.getResponseCode();
            if (code != 200 && code != 401) {
                System.out.println("code = " + code);
                throw new DaltonInvalidResponseException();
            }
            stream = uc.getInputStream();
            response = IOUtils.toString(stream, "UTF-8");

            uc.disconnect();
        } catch (MalformedURLException e) {
            throw new DaltonRuntimeException("Url is malformed", e);
        } catch (SSLException e) {
            throw new DaltonRuntimeException("Url SSL stream kon niet worden geopend", e);
        } catch (IOException e) {
            throw new DaltonRuntimeException("Url stream kon niet worden geopend", e);
        } finally {
            if (stream != null) {
                try {
                    stream.close();
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
            if (uc != null) {
                uc.disconnect();
            }
        }
        return response;
    }

    private static URLConnection openConnectionForUrl(String url)
            throws DaltonRuntimeException, DaltonInvalidResponseException {
        URL u;
        try {
            u = new URL(url);
            System.out.println("Open connection for URL: " + u);
            HttpURLConnection uc = (HttpURLConnection) u.openConnection();
            uc.setConnectTimeout(3000);
            uc.setReadTimeout(3000);
            uc.setUseCaches(false);
            uc.setDefaultUseCaches(false);
            // HttpURLConnection httpConnection = (HttpURLConnection) uc;
            // int code = httpConnection.getResponseCode();
            // if (code != 200 && code != 401) {
            // System.out.println("code = " + code);
            // throw new DaltonInvalidResponseException();
            // }
            return uc; // httpConnection;
        } catch (MalformedURLException e) {
            throw new DaltonRuntimeException("Malformed url", e);
        } catch (IOException e) {
            throw new DaltonRuntimeException("Url IO exception", e);
        }
    }

    private static String constructAuthHeader(String username, String password) {
        String userpass = username + ":" + password;
        return "Basic " + new String(new Base64().encode(userpass.getBytes()));
    }

    private static void processAppInfo(String responseBody, List<String> rowCols) {
        if (StringUtils.isNotBlank(responseBody)) {
            if (StringUtils.startsWith(responseBody, "Error:")) {
                System.out.println(responseBody); // status toevoegen.
            } else if (StringUtils.equals(responseBody, ERR)) {
                rowCols.add(ERR);
            } else {

                int io = StringUtils.indexOf(responseBody, ",");
                int ln = StringUtils.indexOf(responseBody, LINE_FEED);

                String version = StringUtils.substring(responseBody, io + 2, ln);
                extracted(responseBody, rowCols, io);
                rowCols.add(version);
                // System.out.println("Subs: " + substring);
            }
        }

    }

    private static void extracted(String responseBody, List<String> rowCols, int io) {
        if (APP.equals(rowCols.get(0))) {
            String appName = StringUtils.substring(responseBody, 24, io);
            rowCols.set(0, appName);
        }
    }

    private static void printReport() throws DaltonRuntimeException {
        for (String row : rows) {
            // TODO _ pgl sysouts verwijderen.
            System.out.println(row);
        }
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(new File("daltonsVersions.txt"));
            for (String row : rows) {
                row = row + LINE_FEED;
                fos.write(row.getBytes());
            }
        } catch (FileNotFoundException e) {
            throw new DaltonRuntimeException("File not found", e);
        } catch (IOException e) {
            throw new DaltonRuntimeException("IO exception", e);
        } finally {
            try {
                fos.close();
            } catch (IOException e) {
                throw new DaltonRuntimeException("IO exception closing stream", e);
            }
        }

    }

}