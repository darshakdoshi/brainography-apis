package com.brainography.AwsS3;

import com.amazonaws.SdkClientException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSCredentialsProvider;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.*;
import com.brainography.Constant;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

/**
 * @author A SARANG KUMAR TAK
 * @since 07/19/2020
 **/

public class S3repo {

    private static final String SUFFIX = "/";

    protected static AmazonS3 s3client = null;

    public S3repo(){
        if(s3client == null){
            s3client = getAmazonS3Client();
        }
    }

    protected AmazonS3 getAmazonS3Client() {
        AWSCredentials credentials = getCredentials(Constant.accessKey, Constant.SecretAccessKey);
        AWSCredentialsProvider credProvider = new AWSStaticCredentialsProvider(credentials);
        AmazonS3 client = AmazonS3ClientBuilder.standard()
                .withCredentials(credProvider)
                .withRegion(Regions.US_WEST_2)
                .build();
        return client;
    }

    protected AWSCredentials getCredentials(String accessKey, String secretAccessKey) {
        AWSCredentials credentials = new BasicAWSCredentials(accessKey,secretAccessKey);
        return credentials;
    }

    public void createBucket(String bucketName) throws AmazonS3Exception {
        s3client.createBucket(bucketName);
        s3client.setBucketAcl(bucketName, CannedAccessControlList.Private);
        s3client.setPublicAccessBlock(new SetPublicAccessBlockRequest()
                .withBucketName(bucketName)
                .withPublicAccessBlockConfiguration(new PublicAccessBlockConfiguration()
                        .withBlockPublicAcls(true)
                        .withIgnorePublicAcls(true)
                        .withBlockPublicPolicy(true)
                        .withRestrictPublicBuckets(true)));
    }

    public boolean isBucketExist(String bucketName) throws SdkClientException {
        return s3client.doesBucketExistV2(bucketName);
    }

    public void createFolder(String bucketName, String folderName) throws SdkClientException {
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(0);
        InputStream emptyContent = new ByteArrayInputStream(new byte[0]);
        PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName,
                folderName + SUFFIX, emptyContent, metadata);
        s3client.putObject(putObjectRequest);
    }

    public void uploadDocuments(String bucketName, String fileName, InputStream is, ObjectMetadata metadata) throws SdkClientException {
        s3client.putObject(bucketName, fileName, is, metadata);
        s3client.setObjectAcl(bucketName, fileName, CannedAccessControlList.Private);
    }

    public S3ObjectInputStream getObject(GetObjectRequest getObjectRequest) throws SdkClientException {
        return s3client.getObject(getObjectRequest).getObjectContent();
    }

}
