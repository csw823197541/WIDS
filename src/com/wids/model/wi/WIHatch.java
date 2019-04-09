package com.wids.model.wi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by csw on 2018/1/29.
 * Description:
 */
public class WIHatch {

    private Long hatchId;
    private long sentSeq;
    private long workTime;

    private List<Integer> aboveDiscRowNoSeqList;
    private List<Integer> belowDiscRowNoSeqList;
    private List<Integer> belowLoadRowNoSeqList;
    private List<Integer> aboveLoadRowNoSeqList;

    private Map<Integer, List<WICraneMove>> aboveDiscRowNoCraneMoveMap;
    private Map<Integer, List<WICraneMove>> belowDiscRowNoCraneMoveMap;
    private Map<Integer, List<WICraneMove>> aboveLoadRowNoCraneMoveMap;
    private Map<Integer, List<WICraneMove>> belowLoadRowNoCraneMoveMap;

    public WIHatch(Long hatchId) {
        this.hatchId = hatchId;
        sentSeq = 1;
        workTime = 0;
        aboveDiscRowNoSeqList = new ArrayList<>();
        belowDiscRowNoSeqList = new ArrayList<>();
        belowLoadRowNoSeqList = new ArrayList<>();
        aboveLoadRowNoSeqList = new ArrayList<>();
        aboveDiscRowNoCraneMoveMap = new HashMap<>();
        belowDiscRowNoCraneMoveMap = new HashMap<>();
        aboveLoadRowNoCraneMoveMap = new HashMap<>();
        belowLoadRowNoCraneMoveMap = new HashMap<>();
    }

    public Long getHatchId() {
        return hatchId;
    }

    public long getSentSeq() {
        return sentSeq;
    }

    public void setSentSeq(long sentSeq) {
        this.sentSeq = sentSeq;
    }

    public long getWorkTime() {
        return workTime;
    }

    public void setWorkTime(long workTime) {
        this.workTime = workTime;
    }

    public List<Integer> getAboveDiscRowNoSeqList() {
        return aboveDiscRowNoSeqList;
    }

    public void setAboveDiscRowNoSeqList(List<Integer> aboveDiscRowNoSeqList) {
        this.aboveDiscRowNoSeqList = aboveDiscRowNoSeqList;
    }

    public List<Integer> getBelowDiscRowNoSeqList() {
        return belowDiscRowNoSeqList;
    }

    public void setBelowDiscRowNoSeqList(List<Integer> belowDiscRowNoSeqList) {
        this.belowDiscRowNoSeqList = belowDiscRowNoSeqList;
    }

    public List<Integer> getBelowLoadRowNoSeqList() {
        return belowLoadRowNoSeqList;
    }

    public void setBelowLoadRowNoSeqList(List<Integer> belowLoadRowNoSeqList) {
        this.belowLoadRowNoSeqList = belowLoadRowNoSeqList;
    }

    public List<Integer> getAboveLoadRowNoSeqList() {
        return aboveLoadRowNoSeqList;
    }

    public void setAboveLoadRowNoSeqList(List<Integer> aboveLoadRowNoSeqList) {
        this.aboveLoadRowNoSeqList = aboveLoadRowNoSeqList;
    }

    public Map<Integer, List<WICraneMove>> getAboveDiscRowNoCraneMoveMap() {
        return aboveDiscRowNoCraneMoveMap;
    }

    public void addAboveDiscRowNoCraneMoveMap(WICraneMove wiCraneMove) {
        if (aboveDiscRowNoCraneMoveMap.get(wiCraneMove.getRowNo()) == null) {
            aboveDiscRowNoCraneMoveMap.put(wiCraneMove.getRowNo(), new ArrayList<WICraneMove>());
        }
        aboveDiscRowNoCraneMoveMap.get(wiCraneMove.getRowNo()).add(wiCraneMove);
    }

    public Map<Integer, List<WICraneMove>> getBelowDiscRowNoCraneMoveMap() {
        return belowDiscRowNoCraneMoveMap;
    }

    public void addBelowDiscRowNoCraneMoveMap(WICraneMove wiCraneMove) {
        if (belowDiscRowNoCraneMoveMap.get(wiCraneMove.getRowNo()) == null) {
            belowDiscRowNoCraneMoveMap.put(wiCraneMove.getRowNo(), new ArrayList<WICraneMove>());
        }
        belowDiscRowNoCraneMoveMap.get(wiCraneMove.getRowNo()).add(wiCraneMove);
    }

    public Map<Integer, List<WICraneMove>> getAboveLoadRowNoCraneMoveMap() {
        return aboveLoadRowNoCraneMoveMap;
    }

    public void addAboveLoadRowNoCraneMoveMap(WICraneMove wiCraneMove) {
        if (aboveLoadRowNoCraneMoveMap.get(wiCraneMove.getRowNo()) == null) {
            aboveLoadRowNoCraneMoveMap.put(wiCraneMove.getRowNo(), new ArrayList<WICraneMove>());
        }
        aboveLoadRowNoCraneMoveMap.get(wiCraneMove.getRowNo()).add(wiCraneMove);
    }

    public Map<Integer, List<WICraneMove>> getBelowLoadRowNoCraneMoveMap() {
        return belowLoadRowNoCraneMoveMap;
    }

    public void addBelowLoadRowNoCraneMoveMap(WICraneMove wiCraneMove) {
        if (belowLoadRowNoCraneMoveMap.get(wiCraneMove.getRowNo()) == null) {
            belowLoadRowNoCraneMoveMap.put(wiCraneMove.getRowNo(), new ArrayList<WICraneMove>());
        }
        belowLoadRowNoCraneMoveMap.get(wiCraneMove.getRowNo()).add(wiCraneMove);
    }
}
