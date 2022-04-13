package com.vaiv.restFull.domain;

public class BatchServiceState {

    private Long batchServiceRequestSequencePk;
    private String batchState;

    public BatchServiceState() {
    }

    public BatchServiceState(Long batchServiceRequestSequencePk, String batchState) {
        this.batchState = batchState;
        this.batchServiceRequestSequencePk = batchServiceRequestSequencePk;
    }

    public String getBatchState() {
        return batchState;
    }

    public void setBatchState(String batchState) {
        this.batchState = batchState;
    }

    public Long getBatchServiceRequestSequencePk() {
        return batchServiceRequestSequencePk;
    }

    public void setBatchServiceRequestSequencePk(Long batchServiceRequestSequencePk) {
        this.batchServiceRequestSequencePk = batchServiceRequestSequencePk;
    }

    @Override
    public String toString() {
        return "BatchServiceState{" +
                "batchState=" + batchState +
                ", batchServiceRequestSequencePk=" + batchServiceRequestSequencePk +
                '}';
    }
}
