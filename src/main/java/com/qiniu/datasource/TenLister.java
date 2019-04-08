package com.qiniu.datasource;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.COSObjectSummary;
import com.qcloud.cos.model.ListObjectsRequest;
import com.qcloud.cos.model.ObjectListing;
import com.qiniu.common.SuitsException;

import java.util.List;
import java.util.stream.Collectors;

public class TenLister implements ILister<COSObjectSummary> {

    private COSClient cosClient;
    private String endPrefix;
    private ListObjectsRequest listObjectsRequest;
    private ObjectListing objectListing;
    private List<COSObjectSummary> cosObjectList;

    public TenLister(COSClient cosClient, String bucket, String prefix, String marker, String endPrefix,
                     String delimiter, int max) throws CosClientException {
        this.cosClient = cosClient;
        this.listObjectsRequest = new ListObjectsRequest();
        listObjectsRequest.setBucketName(bucket);
        listObjectsRequest.setPrefix(prefix);
        listObjectsRequest.setDelimiter(delimiter);
        listObjectsRequest.setMarker(marker);
        listObjectsRequest.setMaxKeys(max);
        this.endPrefix = endPrefix == null ? "" : endPrefix; // 初始值不使用 null，后续设置时可为空，便于判断是否进行过修改
        this.cosObjectList = getListResult();
    }

    public void setPrefix(String prefix) {
        listObjectsRequest.setPrefix(prefix);
    }

    public String getPrefix() {
        return listObjectsRequest.getPrefix();
    }

    public void setMarker(String marker) {
        listObjectsRequest.setMarker(marker);
    }

    public String getMarker() {
        return listObjectsRequest.getMarker();
    }

    @Override
    public void setEndPrefix(String endKeyPrefix) {
        this.endPrefix = endKeyPrefix;
    }

    @Override
    public String getEndPrefix() {
        return endPrefix;
    }

    @Override
    public void setDelimiter(String delimiter) {
        listObjectsRequest.setDelimiter(delimiter);
    }

    @Override
    public String getDelimiter() {
        return listObjectsRequest.getDelimiter();
    }

    @Override
    public void setLimit(int limit) {
        listObjectsRequest.setMaxKeys(limit);
    }

    public int getLimit() {
        return listObjectsRequest.getMaxKeys();
    }

    private List<COSObjectSummary> getListResult() throws CosClientException {
        objectListing = cosClient.listObjects(listObjectsRequest);
        listObjectsRequest.setMarker(objectListing.getNextMarker());
        return objectListing.getObjectSummaries();
    }

    @Override
    public void listForward() throws SuitsException {
        try {
            List<COSObjectSummary> current;
            do {
                current = getListResult();
            } while (current.size() == 0 && hasNext());

            if (endPrefix != null && !"".equals(endPrefix)) {
                cosObjectList = current.stream()
                        .filter(objectSummary -> objectSummary.getKey().compareTo(endPrefix) < 0)
                        .collect(Collectors.toList());
                if (cosObjectList.size() < current.size()) listObjectsRequest.setMarker(null);
            }
        } catch (CosServiceException e) {
            throw new SuitsException(e.getStatusCode(), e.getMessage());
        } catch (Exception e) {
            throw new SuitsException(-1, "failed, " + e.getMessage());
        }
    }

    @Override
    public boolean hasNext() {
        return objectListing.getNextMarker() != null && !"".equals(objectListing.getNextMarker());
    }

    @Override
    public List<COSObjectSummary> currents() {
        return cosObjectList;
    }

    @Override
    public COSObjectSummary currentFirst() {
        return cosObjectList.size() > 0 ? cosObjectList.get(0) : null;
    }

    @Override
    public COSObjectSummary currentLast() {
        return cosObjectList.size() > 0 ? cosObjectList.get(cosObjectList.size() - 1) : null;
    }

    @Override
    public void close() {
        this.cosClient.shutdown();
        this.cosObjectList = null;
    }
}
