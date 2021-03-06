package com.qiniu.interfaces;

import com.qiniu.common.SuitsException;

import java.util.List;

public interface ILister<E> {

    void setEndPrefix(String endPrefix);

    String getEndPrefix();

    void setLimit(int limit);

    void listForward() throws SuitsException;

    boolean hasNext();

    List<E> currents();

    String truncate();

    long count();

    /**
     * 关闭掉使用的资源
     */
    void close();
}
