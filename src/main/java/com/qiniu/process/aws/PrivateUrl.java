package com.qiniu.process.aws;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder.EndpointConfiguration;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.qiniu.interfaces.ILineProcess;
import com.qiniu.process.Base;
import com.qiniu.util.CloudApiUtils;

import java.io.IOException;
import java.net.URL;
import java.util.Date;
import java.util.Map;

public class PrivateUrl extends Base<Map<String, String>> {

//    private String region;
    private GeneratePresignedUrlRequest request;
    private AmazonS3ClientBuilder amazonS3ClientBuilder;
    private AmazonS3 s3Client;
    private ILineProcess<Map<String, String>> nextProcessor;

    public PrivateUrl(String accessKeyId, String secretKey, String bucket, String endpoint, String region, long expires,
                      Map<String, String> queries) {
        super("awsprivate", accessKeyId, secretKey, bucket);
//        this.region = region;
        request = new GeneratePresignedUrlRequest(bucket, "");
        request.setExpiration(new Date(System.currentTimeMillis() + expires));
        if (queries != null) {
            for (Map.Entry<String, String> entry : queries.entrySet())
                request.addRequestParameter(entry.getKey(), entry.getValue());
        }
        if (endpoint == null || endpoint.isEmpty()) {
            amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretKey)))
                    .withRegion(region);
        } else {
            EndpointConfiguration endpointConfiguration = new EndpointConfiguration(endpoint, region);
            amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretKey)))
                    .withEndpointConfiguration(endpointConfiguration);
        }
        s3Client = amazonS3ClientBuilder.build();
        CloudApiUtils.checkAws(s3Client);
    }

    public PrivateUrl(String accessKeyId, String secretKey, String bucket, String endpoint, String region, long expires,
                      Map<String, String> queries, String savePath, int saveIndex) throws IOException {
        super("awsprivate", accessKeyId, secretKey, bucket, savePath, saveIndex);
//        this.region = region;
        request = new GeneratePresignedUrlRequest(bucket, "");
        request.setExpiration(new Date(System.currentTimeMillis() + expires));
        if (queries != null) {
            for (Map.Entry<String, String> entry : queries.entrySet())
                request.addRequestParameter(entry.getKey(), entry.getValue());
        }
        if (endpoint == null || endpoint.isEmpty()) {
            amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretKey)))
                    .withRegion(region);
        } else {
            EndpointConfiguration endpointConfiguration = new EndpointConfiguration(endpoint, region);
            amazonS3ClientBuilder = AmazonS3ClientBuilder.standard()
                    .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessKeyId, secretKey)))
                    .withEndpointConfiguration(endpointConfiguration);
        }
        s3Client = amazonS3ClientBuilder.build();
        CloudApiUtils.checkAws(s3Client);
    }

    public PrivateUrl(String accessKeyId, String accessKeySecret, String bucket, String endpoint, String region, long expires,
                      Map<String, String> queries, String savePath) throws IOException {
        this(accessKeyId, accessKeySecret, bucket, endpoint, region, expires, queries, savePath, 0);
    }

    public void setNextProcessor(ILineProcess<Map<String, String>> nextProcessor) {
        this.nextProcessor = nextProcessor;
        if (nextProcessor != null) processName = String.join("_with_", nextProcessor.getProcessName(), processName);
    }

    public PrivateUrl clone() throws CloneNotSupportedException {
        PrivateUrl awsPrivateUrl = (PrivateUrl)super.clone();
        awsPrivateUrl.request = (GeneratePresignedUrlRequest) request.clone();
//        awsPrivateUrl.s3Client = AmazonS3ClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials(accessId, secretKey)))
//                .withRegion(region)
//                .build();
        awsPrivateUrl.s3Client = amazonS3ClientBuilder.build();
        if (nextProcessor != null) awsPrivateUrl.nextProcessor = nextProcessor.clone();
        return awsPrivateUrl;
    }

    @Override
    public String resultInfo(Map<String, String> line) {
        return line.get("key");
    }

    @Override
    public String singleResult(Map<String, String> line) throws Exception {
        String key = line.get("key");
        if (key == null) throw new IOException("no key in " + line);
        request.setKey(key);
        URL url = s3Client.generatePresignedUrl(request);
        if (nextProcessor != null) {
            line.put("url", url.toString());
            return nextProcessor.processLine(line);
        }
        return String.join("\t", key, url.toString());
    }

    @Override
    public void closeResource() {
        super.closeResource();
//        region = null;
        request = null;
        amazonS3ClientBuilder = null;
        if (s3Client != null) {
            s3Client.shutdown();
            s3Client = null;
        }
        if (nextProcessor != null) nextProcessor.closeResource();
        nextProcessor = null;
    }
}
