package com.qiniu.entry;

import com.qiniu.common.Zone;
import com.qiniu.model.*;
import com.qiniu.service.interfaces.IOssFileProcess;
import com.qiniu.service.oss.*;
import com.qiniu.storage.Configuration;
import com.qiniu.util.Auth;

import java.util.ArrayList;
import java.util.List;

public class ProcessorChoice {

    public static List<String> unSupportBatch = new ArrayList<String>(){{
        add("asyncfetch");
    }};

    public static IOssFileProcess getFileProcessor(boolean paramFromConfig, String[] args, String configFilePath)
            throws Exception {

        CommonParams commonParams = paramFromConfig ? new CommonParams(configFilePath) : new CommonParams(args);
        String ak = commonParams.getAccessKey();
        String sk = commonParams.getSecretKey();
        String process = commonParams.getProcess();
        boolean batch = commonParams.getProcessBatch();
        if (unSupportBatch.contains(process)) {
            System.out.println(process + " is not support batch operation, it will singly process.");
            batch = false;
        }
        String resultFileDir = commonParams.getResultFileDir();
        IOssFileProcess processor = null;
        Configuration configuration = new Configuration(Zone.autoZone());

        switch (process) {
            case "status": {
                FileStatusParams fileStatusParams = paramFromConfig ?
                        new FileStatusParams(configFilePath) : new FileStatusParams(args);
                processor = new ChangeStatus(Auth.create(ak, sk), configuration, fileStatusParams.getBucket(),
                        fileStatusParams.getTargetStatus(), resultFileDir);
                break;
            }
            case "type": {
                FileTypeParams fileTypeParams = paramFromConfig ?
                        new FileTypeParams(configFilePath) : new FileTypeParams(args);
                processor = new ChangeType(Auth.create(ak, sk), configuration, fileTypeParams.getBucket(),
                        fileTypeParams.getTargetType(), resultFileDir);
                break;
            }
            case "lifecycle": {
                LifecycleParams lifecycleParams = paramFromConfig ?
                        new LifecycleParams(configFilePath) : new LifecycleParams(args);
                processor = new UpdateLifecycle(Auth.create(ak, sk), configuration, lifecycleParams.getBucket(),
                        lifecycleParams.getDays(), resultFileDir);
                break;
            }
            case "copy": {
                FileCopyParams fileCopyParams = paramFromConfig ?
                        new FileCopyParams(configFilePath) : new FileCopyParams(args);
                ak = "".equals(fileCopyParams.getProcessAk()) ? ak : fileCopyParams.getProcessAk();
                sk = "".equals(fileCopyParams.getProcessSk()) ? sk : fileCopyParams.getProcessSk();
                processor = new CopyFile(Auth.create(ak, sk), configuration, fileCopyParams.getBucket(),
                        fileCopyParams.getTargetBucket(), resultFileDir);
                ((CopyFile) processor).setOptions(fileCopyParams.getKeepKey(), fileCopyParams.getKeyPrefix());
                break;
            }
            case "asyncfetch": {
                AsyncFetchParams asyncFetchParams = paramFromConfig ?
                        new AsyncFetchParams(configFilePath) : new AsyncFetchParams(args);
                String accessKey = "".equals(asyncFetchParams.getProcessAk()) ? ak : asyncFetchParams.getProcessAk();
                String secretKey = "".equals(asyncFetchParams.getProcessSk()) ? sk : asyncFetchParams.getProcessSk();
                processor = new AsyncFetch(Auth.create(ak, sk), configuration, asyncFetchParams.getTargetBucket(),
                        asyncFetchParams.getDomain(), resultFileDir);
                ((AsyncFetch) processor).setOptions(asyncFetchParams.getHttps(), asyncFetchParams.getNeedSign() ?
                                Auth.create(accessKey, secretKey) : null, asyncFetchParams.getKeepKey(),
                        asyncFetchParams.getKeyPrefix(), asyncFetchParams.getHashCheck());
                if (asyncFetchParams.hasCustomArgs())
                    ((AsyncFetch) processor).setFetchArgs(asyncFetchParams.getHost(), asyncFetchParams.getCallbackUrl(),
                            asyncFetchParams.getCallbackBody(), asyncFetchParams.getCallbackBodyType(),
                            asyncFetchParams.getCallbackHost(), asyncFetchParams.getFileType(),
                            asyncFetchParams.getIgnoreSameKey());
                break;
            }
        }
        if (processor != null) processor.setBatch(batch);

        return processor;
    }
}