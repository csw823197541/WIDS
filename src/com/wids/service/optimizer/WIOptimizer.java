package com.wids.service.optimizer;

import com.wids.manager.data.WIData;
import com.wids.manager.method.VesselDataMethod;
import com.wids.model.domain.CWPDomain;
import com.wids.model.domain.WIDefaultValue;
import com.wids.model.domain.WIDomain;
import com.wids.model.vessel.VMPosition;
import com.wids.model.wi.*;
import com.wids.service.method.PublicMethod;
import com.wids.service.method.WIDataMethod;

import java.util.*;

/**
 * Created by csw on 2017/10/11.
 * Description:
 */
public class WIOptimizer {

    private long curAreaWorkTime;

    private long oneCntTime = 200000;

    private WIDataMethod wiDataMethod;

    private Map<Long, WIHatch> wiHatchMap;

    public WIOptimizer(WIDataMethod wiDataMethod) {
        this.wiDataMethod = wiDataMethod;
        wiHatchMap = new HashMap<>();
    }

    public void optimizeWi(long firstTime, WIData wiData) {

        this.curAreaWorkTime = firstTime;

        computeCurHourAreaAbility(firstTime, wiData);

        exchangeWi(firstTime, wiData);

    }

    private void computeCurHourAreaAbility(long firstTime, WIData wiData) {
        Set<Long> hatchIdSet = new HashSet<>();
        long intervalTime = 3600000;
        List<WICrane> wiCraneList = wiData.getAllWICraneList();
        WIAreaAbility wiAreaAbilityTemp;
        long workTime;
        int count = 0;
        for (WICrane wiCrane : wiCraneList) {
            wiCrane.setCurExchangeTime(firstTime);
            if (wiCrane.craneCanWork()) {
                workTime = 0;
                List<WIWorkBlock> wiWorkBlockList = wiCrane.getWiWorkBlockList();
                F:
                for (WIWorkBlock wiWorkBlock : wiWorkBlockList) {
                    if (wiWorkBlock.getEstimateStartTime().getTime() > firstTime + intervalTime) {
                        break;
                    }
                    List<WICraneMove> wiCraneMoveList = wiWorkBlock.getWiCraneMoveList();
                    PublicMethod.sortWICraneMoveByMoveOrder(wiCraneMoveList);
                    for (WICraneMove wiCraneMove : wiCraneMoveList) {
                        if (workTime >= intervalTime) {
                            break F;
                        }
                        workTime += wiCraneMove.getCntWorkTime() * 1000;
                        if (CWPDomain.DL_TYPE_LOAD.equals(wiCraneMove.getLdFlag())) {
                            for (WICraneContainer wiCraneContainer : wiCraneMove.getWiCraneContainerList()) {
                                if (wiCraneContainer.getOriginalContainer().getAreaNo() != null) {
                                    wiAreaAbilityTemp = wiData.getWIAreaAbilityByAreaNo(wiCraneContainer.getOriginalContainer().getAreaNo());
                                    if (wiAreaAbilityTemp != null) {
                                        wiAreaAbilityTemp.setCurHourTaskNum(wiAreaAbilityTemp.getCurHourTaskNum() + 1);
                                        if ("20".equals(wiCraneContainer.getOriginalContainer().getSize())) {
                                            wiAreaAbilityTemp.setCurHour20TaskNum(wiAreaAbilityTemp.getCurHour20TaskNum() + 1);
                                        }
                                    }
                                    count += 1;
                                }
                            }
                        }
                        hatchIdSet.add(wiWorkBlock.getHatchId());
                    }
                }
            }
        }
        if (wiData.getAreaAbilityMap().size() > 0) {
            int mean = count / wiData.getAreaAbilityMap().size();
            for (Map.Entry<String, WIAreaAbility> entry : wiData.getAreaAbilityMap().entrySet()) {
                entry.getValue().setCurMeanTaskNum(mean);
                //初始化已经发出去指令的时间
                long st = firstTime;
                entry.getValue().setCurWorkTime(firstTime);
                for (WIContainer wiContainer : entry.getValue().getSentCntList()) {
//                    wiContainer.setWorkingEndTime(new Date(st));
//                    st -= oneCntTime;
//                    wiContainer.setWorkingStartTime(new Date(st));
                    wiContainer.setWorkingStartTime(new Date(st));
                    st += oneCntTime;
                    wiContainer.setWorkingEndTime(new Date(st));
                }
            }
        }
        VesselDataMethod vesselDataMethod = new VesselDataMethod(wiDataMethod.getVesselData());
        String sl = PublicMethod.getOddOrEvenBySeaOrLand(wiData.getVmSchedule().getPlanBerthDirect(), CWPDomain.ROW_SEQ_SEA_LAND);
        String ls = PublicMethod.getOddOrEvenBySeaOrLand(wiData.getVmSchedule().getPlanBerthDirect(), CWPDomain.ROW_SEQ_LAND_SEA);
        for (Long hatchId : hatchIdSet) {
            WIHatch wiHatch = new WIHatch(hatchId);
            //作业顺序
            wiHatch.setAboveDiscRowNoSeqList(vesselDataMethod.getRowSeqListByHatchId(hatchId, CWPDomain.BOARD_ABOVE, ls));
            wiHatch.setBelowDiscRowNoSeqList(vesselDataMethod.getRowSeqListByHatchId(hatchId, CWPDomain.BOARD_BELOW, ls));
            wiHatch.setBelowLoadRowNoSeqList(vesselDataMethod.getRowSeqListByHatchId(hatchId, CWPDomain.BOARD_BELOW, sl));
            wiHatch.setAboveLoadRowNoSeqList(vesselDataMethod.getRowSeqListByHatchId(hatchId, CWPDomain.BOARD_ABOVE, sl));
            //将装船move按槽存储
            List<WICraneMove> wiCraneMoveList = wiData.getWaitWICraneMoveListByHatchId(hatchId); //整个舱待发送的指令
//            PublicMethod.sortWICraneMoveByMoveOrder(wiCraneMoveList);
            for (WICraneMove wiCraneMove : wiCraneMoveList) {
                if (CWPDomain.DL_TYPE_DISC.equals(wiCraneMove.getLdFlag())) {
                    if (wiCraneMove.getTierNo() > 49) {
                        wiHatch.addAboveDiscRowNoCraneMoveMap(wiCraneMove);
                    } else {
                        wiHatch.addBelowDiscRowNoCraneMoveMap(wiCraneMove);
                    }
                }
                if (CWPDomain.DL_TYPE_LOAD.equals(wiCraneMove.getLdFlag())) {
                    if (wiCraneMove.getTierNo() > 49) {
                        wiHatch.addAboveLoadRowNoCraneMoveMap(wiCraneMove);
                    } else {
                        wiHatch.addBelowLoadRowNoCraneMoveMap(wiCraneMove);
                    }
                }
            }
            wiHatchMap.put(hatchId, wiHatch);
        }
    }

    private void exchangeWi(long firstTime, WIData wiData) {
        long intervalTime = wiData.getWiConfiguration().getIntervalTime() * 60000;
        List<WICrane> wiCraneList = wiData.getAllWICraneList();
        long curTime = 0;
        for (; curTime < intervalTime; ) { //交换一个小时的指令
            if (curTime >= intervalTime) {
                break;
            }
            long minWorkTime = Long.MAX_VALUE;
            for (WICrane wiCrane : wiCraneList) {
                try {
                    if (wiCrane.craneCanWork() && wiCrane.getCurExchangeTime() < firstTime + intervalTime) {
                        long wt = exchangeWiByCrane(firstTime, intervalTime, wiCrane, wiData);
                        if (wt > 0) {
                            minWorkTime = Math.min(minWorkTime, wt);
                        }
                    }
                } catch (Exception e) {
                    wiData.getLogger().logInfo("算法交换桥机（craneNo:" + wiCrane.getCraneNo() + "）的作业指令时发生异常");
                    e.printStackTrace();
                }
            }
//            curAreaWorkTime += 120000;
            curAreaWorkTime += minWorkTime;
            //其他箱区时间设置成为当前时间
            for (Map.Entry<String, WIAreaAbility> entry : wiData.getAreaAbilityMap().entrySet()) {
                if (entry.getValue().getCurWorkTime() < curAreaWorkTime) {
                    entry.getValue().setCurWorkTime(curAreaWorkTime);
                }
            }
            curTime += minWorkTime;
        }
    }

    private long exchangeWiByCrane(long firstTime, long intervalTime, WICrane wiCrane, WIData wiData) {
        long wt = 0;
        List<WIWorkBlock> wiWorkBlockList = wiCrane.getWiWorkBlockList();
        for (int i = 0; i < wiWorkBlockList.size(); i++) {
            WIWorkBlock wiWorkBlock = wiWorkBlockList.get(i);
            if (wiWorkBlock.getEstimateStartTime().getTime() > firstTime + intervalTime) {
                break;
            }
            if (wiCrane.getCurExchangeTime() >= firstTime + intervalTime) {
                break;
            }
            //判断桥机上一个指令还没有做完，此时不选择该桥机的指令交换
            if (wiCrane.getCurExchangeTime() > curAreaWorkTime) {
                break;
            }
            //当桥机已交换的指令时间正好是作业块结束时间，跳到下一个作业块发箱
//            if (wiWorkBlock.getEstimateEndTime().getTime() <= wiCrane.getCurExchangeTime()) {
//                continue;
//            }

            int j = i;
            for (; j < wiWorkBlockList.size(); j++) {//判断当前作业块是否做完了，跳到下一个作业
                wiWorkBlock = wiWorkBlockList.get(j);
                long waitT = wiWorkBlock.getWaitWiWorkTime();
                if (waitT > wiWorkBlock.getExCntTime()) {
                    break;
                }
            }
            WICraneMove firstOrderCraneMove = getFirstOrderCraneMove(wiWorkBlock); //根据作业块得到序号最小的move
            if (firstOrderCraneMove != null) {
                String board = firstOrderCraneMove.getTierNo() > 49 ? CWPDomain.BOARD_ABOVE : CWPDomain.BOARD_BELOW;
                if (CWPDomain.DL_TYPE_DISC.equals(firstOrderCraneMove.getLdFlag())) {
                    wt = exchangeDiscWi(wiWorkBlock, wiData, board);
                    if (wt > 0) {
                        wiWorkBlock.setExCntTime(wiWorkBlock.getExCntTime() + wt);
                        break;
                    }
                }
                if (CWPDomain.DL_TYPE_LOAD.equals(firstOrderCraneMove.getLdFlag())) {
                    wt = exchangeLoadWi(wiWorkBlock, wiData, board);
                    if (wt > 0) {
                        wiWorkBlock.setExCntTime(wiWorkBlock.getExCntTime() + wt);
                        break;
                    }
                }
            }
        }
        wiCrane.setCurExchangeTime(wiCrane.getCurExchangeTime() + wt);
        return wt;
    }

    private WICraneMove getFirstOrderCraneMove(WIWorkBlock wiWorkBlock) {
        WIHatch wiHatch = wiHatchMap.get(wiWorkBlock.getHatchId());
        Integer bayNo = Integer.valueOf(wiWorkBlock.getBayNo());
        WICraneMove firstOrderCraneMove = null;
        long maxOrder = Long.MAX_VALUE;
        List<WICraneMove> wiCraneMoveList = new ArrayList<>();
        if (wiHatch != null) {
            for (List<WICraneMove> wiCraneMoves : wiHatch.getAboveDiscRowNoCraneMoveMap().values()) {
                wiCraneMoveList.addAll(wiCraneMoves);
            }
            for (List<WICraneMove> wiCraneMoves : wiHatch.getBelowDiscRowNoCraneMoveMap().values()) {
                wiCraneMoveList.addAll(wiCraneMoves);
            }
            for (List<WICraneMove> wiCraneMoves : wiHatch.getBelowLoadRowNoCraneMoveMap().values()) {
                wiCraneMoveList.addAll(wiCraneMoves);
            }
            for (List<WICraneMove> wiCraneMoves : wiHatch.getAboveLoadRowNoCraneMoveMap().values()) {
                wiCraneMoveList.addAll(wiCraneMoves);
            }
            // 如果没有卸船
            boolean noneDiscFlag = true;
            for (WICraneMove wiCraneMove : wiCraneMoveList) {
                if (CWPDomain.DL_TYPE_DISC.equals(wiCraneMove.getLdFlag()) && wiCraneMove.getSentSeq() >= WIDefaultValue.sentSeq) {
                    noneDiscFlag = false;
                }
            }
            if (noneDiscFlag) {
                // 如果有甲板下的，按甲板下的move算
                List<WICraneMove> wiCraneMoveList1 = new ArrayList<>();
                for (WICraneMove wiCraneMove : wiCraneMoveList) {
                    if (wiCraneMove.getTierNo() < 49 && wiCraneMove.getSentSeq() >= WIDefaultValue.sentSeq) {
                        wiCraneMoveList1.add(wiCraneMove);
                    }
                }
                if (wiCraneMoveList1.size() > 0) {
                    wiCraneMoveList = wiCraneMoveList1;
                }
            }
            for (WICraneMove wiCraneMove : wiCraneMoveList) {
                if (wiCraneMove.getSentSeq() >= WIDefaultValue.sentSeq && wiCraneMove.getWorkBayNo().equals(bayNo) && wiCraneMove.getMoveOrder().compareTo(maxOrder) < 0) {
                    maxOrder = wiCraneMove.getMoveOrder();
                    firstOrderCraneMove = wiCraneMove;
                }
            }
        }
        return firstOrderCraneMove;
    }

    //------------------交换---------------

    private long exchangeDiscWi(WIWorkBlock wiWorkBlock, WIData wiData, String board) {
        long wt = 0;
        WIHatch wiHatch = wiHatchMap.get(wiWorkBlock.getHatchId());
        if (wiHatch != null) {
            Integer bayNo = Integer.valueOf(wiWorkBlock.getBayNo());
            List<WICraneMove> curCanWorkMoves = new ArrayList<>();
            if (CWPDomain.BOARD_ABOVE.equals(board) && wiHatch.getAboveDiscRowNoCraneMoveMap().size() > 0) {
                curCanWorkMoves = getCurCanDiscWorkMoves(wiData, bayNo, wiHatch.getAboveDiscRowNoSeqList(), wiHatch.getAboveDiscRowNoCraneMoveMap());
            } else if (CWPDomain.BOARD_BELOW.equals(board) && wiHatch.getBelowDiscRowNoCraneMoveMap().size() > 0) {
                curCanWorkMoves = getCurCanDiscWorkMoves(wiData, bayNo, wiHatch.getBelowDiscRowNoSeqList(), wiHatch.getBelowDiscRowNoCraneMoveMap());
            }
            if (curCanWorkMoves.size() == 0) {
                return wt;
            } else {
                WICraneMove optimalMove = findOptimalDiscMove(curCanWorkMoves, wiData);
                optimalMove.setSentSeq(wiHatch.getSentSeq());
                for (WICraneContainer wiCraneContainer : optimalMove.getWiCraneContainerList()) {
                    wiCraneContainer.setSentSeq(wiHatch.getSentSeq());
                    wiCraneContainer.getExchangeContainer().setSentSeq(wiHatch.getSentSeq());
                }
                wiHatch.setSentSeq(wiHatch.getSentSeq() + 1);
                wt = optimalMove.getCntWorkTime() * 1000;
            }
        }
        return wt;
    }

    private long exchangeLoadWi(WIWorkBlock wiWorkBlock, WIData wiData, String board) {
        long wt = 0;
        WIHatch wiHatch = wiHatchMap.get(wiWorkBlock.getHatchId());
        if (wiHatch != null) {
            Integer bayNo = Integer.valueOf(wiWorkBlock.getBayNo());
            List<WICraneMove> curCanWorkMoves = new ArrayList<>();
            if (CWPDomain.BOARD_BELOW.equals(board) && wiHatch.getBelowLoadRowNoCraneMoveMap().size() > 0) {
                curCanWorkMoves = getBelowCurCanLoadWorkMoves(wiData, bayNo, wiHatch.getBelowLoadRowNoSeqList(), wiHatch.getBelowLoadRowNoCraneMoveMap());
            } else if (CWPDomain.BOARD_ABOVE.equals(board) && wiHatch.getAboveLoadRowNoCraneMoveMap().size() > 0) {
                curCanWorkMoves = getAboveCurCanLoadWorkMoves(wiData, bayNo, wiHatch.getAboveLoadRowNoSeqList(), wiHatch.getAboveLoadRowNoCraneMoveMap());
            }
            if (curCanWorkMoves.size() == 0) {
                return wt;
            } else {
                WICraneMove optimalMove = findOptimalLoadMove(curCanWorkMoves, wiData);
                optimalMove.setSentSeq(wiHatch.getSentSeq());
                for (WICraneContainer wiCraneContainer : optimalMove.getWiCraneContainerList()) {
                    wiCraneContainer.setExchangeContainer(wiCraneContainer.getExchangeContainerTemp());
                    wiCraneContainer.setSentSeq(wiHatch.getSentSeq());
                    wiCraneContainer.setExchangeReason(optimalMove.getWiExchangeValue().getDesc());
                    WIContainer exchangeCnt = wiCraneContainer.getExchangeContainer(); //发送的是哪个箱子
                    exchangeCnt.setSentSeq(wiHatch.getSentSeq());
                    WIAreaAbility wiAreaAbility = wiData.getWIAreaAbilityByAreaNo(exchangeCnt.getAreaNo());
                    if (wiAreaAbility != null) {
                        wiAreaAbility.setCurHourSentNum(wiAreaAbility.getCurHourSentNum() + 1);
                        wiCraneContainer.setOverCntNum(exchangeCnt.getOverCntNumTemp());
                        //将发送的箱子放到已发队列中
                        long st = wiAreaAbility.getCurWorkTime();
                        exchangeCnt.setWorkingStartTime(new Date(st));
                        st += oneCntTime;
                        exchangeCnt.setWorkingEndTime(new Date(st));
                        wiAreaAbility.setCurWorkTime(st);
                        wiAreaAbility.getSentCntList().add(exchangeCnt);
                    }
                    //保存交换的结果
                    if (wiAreaAbility != null) {
                        saveExchangeCntInfo(wiCraneContainer, wiData);
                    }
                }
                wiHatch.setSentSeq(wiHatch.getSentSeq() + 1);
                wt = optimalMove.getCntWorkTime() * 1000;
            }
        }
        return wt;
    }

    //------------------卸船策略方法---------------

    private List<WICraneMove> getCurCanDiscWorkMoves(WIData wiData, Integer bayNo, List<Integer> rowNoSeqList, Map<Integer, List<WICraneMove>> rowNoCraneMoveMap) {
        List<WICraneMove> wiCraneMoves = new ArrayList<>();
        WICraneMove frontFirstMove = null;
        long firstOrder = Long.MAX_VALUE;
        for (Integer rowNo : rowNoSeqList) {
            WICraneMove firstMove = getFirstMove(rowNo, bayNo, rowNoCraneMoveMap, CWPDomain.DL_TYPE_DISC);
            if (firstMove != null && !WIDomain.YES.equals(firstMove.getCwoManualWi())) {
                if (WIDomain.NO.equals(wiData.getWiConfiguration().getMoveOrderExchange())) { //不允许交换顺序
                    if (firstMove.getMoveOrder() < firstOrder) {
                        firstOrder = firstMove.getMoveOrder();
                        frontFirstMove = firstMove;
                    }
                } else {
                    //可以交换顺序，根据策略卸船（默认按原始顺序卸船）
                    if (firstMove.getMoveOrder() < firstOrder) {
                        firstOrder = firstMove.getMoveOrder();
                        frontFirstMove = firstMove;
                    }
                }
            }
//            if (firstMove != null && !WIDomain.YES.equals(firstMove.getCwoManualWi())) {
//                if (frontFirstMove != null) {
//                    if (frontFirstMove.getTierNo() < firstMove.getTierNo()) {
//                        wiCraneMoves.add(firstMove);
//                    }
//                } else {
//                    wiCraneMoves.add(firstMove);
//                }
//                frontFirstMove = firstMove;
//            }
        }
        if (frontFirstMove != null) {
            wiCraneMoves.add(frontFirstMove);
        }
        return wiCraneMoves;
    }

    private WICraneMove findOptimalDiscMove(List<WICraneMove> curCanWorkMoves, WIData wiData) {
        if (curCanWorkMoves.size() == 1) {
            return curCanWorkMoves.get(0);
        } else {
            WICraneMove optimalMove;
            //按关号排序策略
            PublicMethod.sortWICraneMoveByMoveOrder(curCanWorkMoves);
            optimalMove = curCanWorkMoves.get(0);
            return optimalMove;
        }
    }

    //------------------装船策略方法---------------

    private List<WICraneMove> getBelowCurCanLoadWorkMoves(WIData wiData, Integer bayNo, List<Integer> rowNoSeqList, Map<Integer, List<WICraneMove>> rowNoCraneMoveMap) {
        //通过策略控制返回当前可作业的位置，甲板下可作业的位置更多
        List<WICraneMove> curCanWorkMoves = new ArrayList<>();
        WICraneMove frontFirstMove = null;
        long firstOrder = Long.MAX_VALUE;
        for (int i = 0; i < rowNoSeqList.size(); i++) {
            Integer rowNo = rowNoSeqList.get(i);
            WICraneMove firstMove = getFirstMove(rowNo, bayNo, rowNoCraneMoveMap, CWPDomain.DL_TYPE_LOAD);
            if (firstMove != null && PublicMethod.canLoadState(firstMove.getWorkStatus()) && !WIDomain.YES.equals(firstMove.getCwoManualWi())) {
                if (WIDomain.NO.equals(wiData.getWiConfiguration().getMoveOrderExchange())) { //不允许交换顺序
                    if (firstMove.getMoveOrder() < firstOrder) {
                        firstOrder = firstMove.getMoveOrder();
                        frontFirstMove = firstMove;
                    }
                } else {
                    curCanWorkMoves.add(firstMove);
                }
            }
        }
        if (WIDomain.NO.equals(wiData.getWiConfiguration().getMoveOrderExchange()) && frontFirstMove != null) {
            curCanWorkMoves.add(frontFirstMove);
        }
        //todo:怎么控制作业工艺的连续性
        if (curCanWorkMoves.size() > 1) {
            return limitTierHeight(curCanWorkMoves, wiData);
        }
        return curCanWorkMoves;
    }

    private List<WICraneMove> getAboveCurCanLoadWorkMoves(WIData wiData, Integer bayNo, List<Integer> rowNoSeqList, Map<Integer, List<WICraneMove>> rowNoCraneMoveMap) {
        //通过策略控制返回当前可作业的位置，甲板上可作业的位置要少些
        List<WICraneMove> curCanWorkMoves = new ArrayList<>();
        WICraneMove frontFirstMove = null;
        long firstOrder = Long.MAX_VALUE;
        for (int i = 0; i < rowNoSeqList.size(); i++) {
            Integer rowNo = rowNoSeqList.get(i);
            WICraneMove firstMove = getFirstMove(rowNo, bayNo, rowNoCraneMoveMap, CWPDomain.DL_TYPE_LOAD);
            if (firstMove != null && PublicMethod.canLoadState(firstMove.getWorkStatus()) && !WIDomain.YES.equals(firstMove.getCwoManualWi())) {
                if (WIDomain.NO.equals(wiData.getWiConfiguration().getMoveOrderExchange())) { //不允许交换顺序
                    if (firstMove.getMoveOrder() < firstOrder) {
                        firstOrder = firstMove.getMoveOrder();
                        frontFirstMove = firstMove;
                    }
                } else {
                    if (frontFirstMove != null) {
                        if (frontFirstMove.getTierNo() > firstMove.getTierNo()) {
                            curCanWorkMoves.add(firstMove);
                        }
                    } else {
                        curCanWorkMoves.add(firstMove);
                    }
                    frontFirstMove = firstMove;
                }
            }
        }
        if (WIDomain.NO.equals(wiData.getWiConfiguration().getMoveOrderExchange()) && frontFirstMove != null) {
            curCanWorkMoves.add(frontFirstMove);
        }
        //todo: 怎么控制20尺和40尺箱子交替发箱、怎么控制按舱盖板分档发箱???
        if (curCanWorkMoves.size() > 1) {
            return limitTierHeight(curCanWorkMoves, wiData);
        }
        return curCanWorkMoves;
    }

    private WICraneMove findOptimalLoadMove(List<WICraneMove> curCanWorkMoves, WIData wiData) {
        PublicMethod.sortWICraneMoveByMoveOrder(curCanWorkMoves);
        //设置优先级：1、满足箱区出箱能力且不翻箱优先发箱（10）；刚回收没场箱位的箱子优先发箱（10）；不允许交换的箱子，按关号发箱（10）
        // 2、交换后，（20、21、22）；
        for (WICraneMove wiCraneMove : curCanWorkMoves) {
            Integer valueTemp = 201; //交换后的箱子发生翻箱，不交换按关号发箱
            WIExchangeValue exchangeValue = new WIExchangeValue(valueTemp);
            exchangeValue.setDesc("交换后的箱子发生翻箱，没有选择交换的箱子发箱");
            if (PublicMethod.canLoadState(wiCraneMove.getWorkStatus())) { //状态是S，可以装船状态
                if ("1".equals(wiCraneMove.getWorkflow()) && wiCraneMove.getWiCraneContainerList().size() == 1) {
                    WICraneContainer wiCraneContainer = wiCraneMove.getWiCraneContainerList().get(0);
                    WIContainer wiContainer = wiCraneContainer.getExchangeContainer();
                    if (!notExchangeCnt(wiContainer)) {
                        WIAreaAbility wiAreaAbility = wiData.getWIAreaAbilityByAreaNo(wiContainer.getAreaNo());
                        if (wiAreaAbility != null) {
                            int sentWiNum = wiAreaAbility.getSentWiNumBy15Mis(curAreaWorkTime, wiData.getWiConfiguration().getSendIntervalTime());
                            boolean needExchange = sentWiNum >= wiData.getWiConfiguration().getSendContainerNum();
                            int overCntNum = getOverCntNum(wiContainer, wiAreaAbility.getWiContainerList());
                            wiCraneContainer.setOriginalOverCntNum(overCntNum);
                            if (!needExchange && overCntNum == 0) { //满足箱区能力，没有翻箱
                                valueTemp = 10;
                                exchangeValue.setCode(valueTemp);
                                exchangeValue.setDesc("满足箱区出箱能力且不翻箱，优先被选中发箱");
                            } else {
                                List<WIAreaAbility> wiAreaAbilityList = findCurCanExchangeArea(wiData);
                                if (wiAreaAbilityList.size() > 0) {
                                    List<WIContainer> exchangeCntList = findOptimalSingleExchangeCntList(wiCraneContainer, wiAreaAbilityList, wiData);
                                    if (exchangeCntList.size() > 0) { //todo:暂时取翻箱量最少的一个
                                        WIContainer exCnt = exchangeCntList.get(0);
                                        int exOverNum = exCnt.getOverCntNumTemp();
                                        WIAreaAbility exAreaAbility = wiData.getWIAreaAbilityByAreaNo(exCnt.getAreaNo());
                                        if (exAreaAbility != null) {
                                            int exSentWiNum = exAreaAbility.getSentWiNumBy15Mis(curAreaWorkTime, wiData.getWiConfiguration().getSendIntervalTime());
                                            if (exSentWiNum + exOverNum < sentWiNum) {
                                                valueTemp = 20 + exOverNum;
                                            } else {
                                                valueTemp += sentWiNum;
                                            }
                                        }
                                        wiCraneContainer.setExchangeContainerTemp(exCnt);
                                        exchangeValue.setCode(valueTemp);
                                        exchangeValue.setDesc("找到适合交换的箱子(交换后箱子的翻箱量：" + String.valueOf(exOverNum) + ")");
                                    } else { //没有找到适合(满足参数/属性组/箱区能力)交换的箱子，按关号发箱
                                        valueTemp += sentWiNum;
                                        exchangeValue.setCode(valueTemp);
                                        exchangeValue.setDesc("没有找到适合交换的箱子(箱区任务：" + sentWiNum + ")，按关号发箱");
                                    }
                                } else { //没找到满足出箱能力的箱区，按关号发箱
                                    valueTemp += sentWiNum;
                                    exchangeValue.setCode(valueTemp);
                                    exchangeValue.setDesc("没找到满足出箱能力的箱区(箱区任务：" + sentWiNum + ")，按关号发箱");
                                }
                            }
                        } else { //一般都是AGV、ASC、RACK上没场箱位，刚回收的箱子
                            valueTemp = 10;
                            exchangeValue.setCode(valueTemp);
                            exchangeValue.setDesc("没有场箱位的箱子，按关号发箱");
                        }
                    } else { //不允许交换的箱子
                        valueTemp = 10;
                        exchangeValue.setCode(valueTemp);
                        exchangeValue.setDesc("不允许交换的箱子，按关号发箱");
                    }
                } else if ("2".equals(wiCraneMove.getWorkflow()) && wiCraneMove.getWiCraneContainerList().size() == 2) {
                    WICraneContainer wiCraneContainer1 = wiCraneMove.getWiCraneContainerList().get(0);
                    WICraneContainer wiCraneContainer2 = wiCraneMove.getWiCraneContainerList().get(1);
                    WIContainer wiContainer1 = wiCraneContainer1.getExchangeContainer();
                    WIContainer wiContainer2 = wiCraneContainer2.getExchangeContainer();
                    WIAreaAbility wiAreaAbility1 = wiData.getWIAreaAbilityByAreaNo(wiContainer1.getAreaNo());
                    WIAreaAbility wiAreaAbility2 = wiData.getWIAreaAbilityByAreaNo(wiContainer2.getAreaNo());
                    if (wiAreaAbility1 != null && wiAreaAbility2 != null) { //一般都是AGV、ASC、RACK上没场箱位，刚回收的箱子
                        int sentWiNum1 = wiAreaAbility1.getSentWiNumBy15Mis(curAreaWorkTime, wiData.getWiConfiguration().getSendIntervalTime());
                        int sentWiNum2 = wiAreaAbility2.getSentWiNumBy15Mis(curAreaWorkTime, wiData.getWiConfiguration().getSendIntervalTime());
                        boolean notExchange = notExchangeCnt(wiContainer1) || notExchangeCnt(wiContainer2);
                        if (!notExchange) { //双箱吊可以交换
                            List<WIAreaAbility> wiAreaAbilityList = new ArrayList<>();
                            if (!wiContainer1.getAreaNo().equals(wiContainer2.getAreaNo())) { //拼箱
                                if (sentWiNum1 < sentWiNum2) {
                                    wiAreaAbilityList.add(wiAreaAbility1);
                                } else if (sentWiNum2 < sentWiNum1) {
                                    wiAreaAbilityList.add(wiAreaAbility2);
                                } else {
                                    if (sentWiNum1 == 0) { //箱区当前没有任务
                                        if (wiAreaAbility1.getCurHour20TaskNum() < wiAreaAbility2.getCurHour20TaskNum()) {
                                            wiAreaAbilityList.add(wiAreaAbility2);
                                        } else {
                                            wiAreaAbilityList.add(wiAreaAbility1);
                                        }
                                    } else {
                                        wiAreaAbilityList = findCurCanExchangeArea(wiData);
                                    }
                                }
                            } else { //不拼箱
                                if (sentWiNum1 >= wiData.getWiConfiguration().getSendContainerNum()) {//箱区能力超过4交换
                                    wiAreaAbilityList = findCurCanExchangeArea(wiData);
                                }
                            }
                            boolean needExchange = sentWiNum1 >= wiData.getWiConfiguration().getSendContainerNum();
                            if (!needExchange) {
                                valueTemp = 10;
                                exchangeValue.setCode(valueTemp);
                                exchangeValue.setDesc("满足箱区出箱能力且不翻箱，优先被选中发箱");
                            } else {
                                if (wiAreaAbilityList.size() > 0) { //交换双箱吊的箱子
                                    List<WIContainer> exchangeCntList = findOptimal20DoubleExchangeCntList(wiCraneContainer1, wiCraneContainer2, wiAreaAbilityList, wiData);
                                    if (exchangeCntList.size() == 2) {
                                        int overNum = exchangeCntList.get(0).getOverCntNumTemp() + exchangeCntList.get(1).getOverCntNumTemp();
                                        valueTemp = 20 + overNum;
                                        wiCraneContainer1.setExchangeContainerTemp(exchangeCntList.get(0));
                                        wiCraneContainer2.setExchangeContainerTemp(exchangeCntList.get(1));
                                        exchangeValue.setCode(valueTemp);
                                        exchangeValue.setDesc("找到适合交换的箱子(交换后箱子的翻箱量：" + String.valueOf(overNum));
                                    } else { //没有找到适合(满足参数/属性组/箱区能力)交换的箱子，按关号发箱
                                        valueTemp += sentWiNum1;
                                        exchangeValue.setCode(valueTemp);
                                        exchangeValue.setDesc("没有找到适合交换的箱子(箱区任务：" + sentWiNum1 + ")，按关号发箱");
                                    }
                                } else { //没找到满足出箱能力的箱区，按关号发箱
                                    valueTemp += sentWiNum1;
                                    exchangeValue.setCode(valueTemp);
                                    exchangeValue.setDesc("没找到满足出箱能力的箱区(箱区任务：" + sentWiNum1 + ")，按关号发箱");
                                }
                            }
                        } else {
                            valueTemp = 10;
                            exchangeValue.setCode(valueTemp);
                            exchangeValue.setDesc("不允许交换的箱子，按关号发箱");
                        }
                    }
                }
            }
            wiCraneMove.setValueTemp(valueTemp);
            wiCraneMove.setWiExchangeValue(exchangeValue);
        }
        Collections.sort(curCanWorkMoves, new Comparator<WICraneMove>() {
            @Override
            public int compare(WICraneMove o1, WICraneMove o2) {
                if (o1.getValueTemp().compareTo(o2.getValueTemp()) == 0) {
                    return o1.getMoveOrder().compareTo(o2.getMoveOrder());
                } else {
                    return o1.getValueTemp().compareTo(o2.getValueTemp());
                }
            }
        });
        WICraneMove optimalMove = curCanWorkMoves.get(0);
        for (int i = 1; i < curCanWorkMoves.size(); i++) {//没选中的数据还原
            curCanWorkMoves.get(i).setValueTemp(null);
            curCanWorkMoves.get(i).setWiExchangeValue(null);
            for (WICraneContainer wiCraneContainer : curCanWorkMoves.get(i).getWiCraneContainerList()) {
                wiCraneContainer.setExchangeContainerTemp(wiCraneContainer.getExchangeContainer());
            }
        }
        return optimalMove;
    }

    //------------------查找交换的箱子---------------

    private List<WIAreaAbility> findCurCanExchangeArea(WIData wiData) {
        List<WIAreaAbility> wiAreaAbilities = new ArrayList<>();
        for (Map.Entry<String, WIAreaAbility> entry : wiData.getAreaAbilityMap().entrySet()) {
            if (entry.getValue().getAllTaskNum() > entry.getValue().getCurMeanTaskNum()) {
                if (!PublicMethod.specialArea(entry.getKey()) && entry.getValue().getSentWiNumBy15Mis(curAreaWorkTime, wiData.getWiConfiguration().getSendIntervalTime()) < wiData.getWiConfiguration().getSendContainerNum()) {
                    wiAreaAbilities.add(entry.getValue());
                }
            }
        }
        //todo:选择一个当前指令任务最少，且总任务最多的箱区
//        Collections.sort(wiAreaAbilities, new Comparator<WIAreaAbility>() {
//            @Override
//            public int compare(WIAreaAbility o1, WIAreaAbility o2) {
//                Integer curTN1 = o1.getCurHourTaskNum() + o1.getCurHourExchangeNum();
//                Integer curTN2 = o2.getCurHourTaskNum() + o2.getCurHourExchangeNum();
//                if (curTN1.compareTo(curTN2) == 0) {
//                    return o2.getAllTaskNum().compareTo(o1.getAllTaskNum());
//                } else {
//                    return curTN1.compareTo(curTN2);
//                }
//            }
//        });
        return wiAreaAbilities;
    }

    private List<WIContainer> findOptimalSingleExchangeCntList(WICraneContainer wiCraneContainer, List<WIAreaAbility> wiAreaAbilityList, WIData wiData) {
        List<WIContainer> wiContainers = new ArrayList<>();
        for (WIAreaAbility wiAreaAbility : wiAreaAbilityList) {
            for (WIContainer cnt : wiAreaAbility.getUnSendCntList()) { //从未发送的箱子中挑选
                if (cnt.getyLocation() != null) { //排除AGV、ASC、RACK上没场箱位，刚回收的箱子
                    if (cnt.getSentSeq() == null && !cnt.getyLocation().equals(wiCraneContainer.getExchangeContainer().getyLocation())) {
                        if (conformRules(wiCraneContainer, cnt, wiData)) { //满足规则
                            int overCntNum = getOverCntNum(cnt, wiAreaAbility.getWiContainerList());
                            cnt.setOverCntNumTemp(overCntNum);
                            wiContainers.add(cnt);
                        }
                    }
                }
            }
        }
        if (wiContainers.size() > 0) {
            Collections.sort(wiContainers, new Comparator<WIContainer>() {
                @Override
                public int compare(WIContainer o1, WIContainer o2) {
                    return o1.getOverCntNumTemp().compareTo(o2.getOverCntNumTemp());
                }
            });
        }
        return wiContainers;
    }

    private List<WIContainer> findOptimal20DoubleExchangeCntList(WICraneContainer wiCraneContainer1, WICraneContainer wiCraneContainer2, List<WIAreaAbility> wiAreaAbilityList, WIData wiData) {
        List<WIContainer> wiContainerList = new ArrayList<>();
        List<WIContainer> exCntList1 = new ArrayList<>();
        List<WIContainer> exCntList2 = new ArrayList<>();
        WIAreaAbility wiAreaAbility1 = wiData.getWIAreaAbilityByAreaNo(wiCraneContainer1.getExchangeContainer().getAreaNo());
        WIAreaAbility wiAreaAbility2 = wiData.getWIAreaAbilityByAreaNo(wiCraneContainer2.getExchangeContainer().getAreaNo());
        int overCntNum1 = getOverCntNum(wiCraneContainer1.getExchangeContainer(), wiAreaAbility1.getWiContainerList());
        int overCntNum2 = getOverCntNum(wiCraneContainer2.getExchangeContainer(), wiAreaAbility2.getWiContainerList());
        wiCraneContainer1.setOriginalOverCntNum(overCntNum1);
        wiCraneContainer2.setOriginalOverCntNum(overCntNum2);
        if (!wiAreaAbilityList.contains(wiAreaAbility1)) {
            exCntList1 = findOptimalSingleExchangeCntList(wiCraneContainer1, wiAreaAbilityList, wiData);
        } else {
            wiCraneContainer1.getExchangeContainer().setOverCntNumTemp(overCntNum1);
            exCntList1.add(wiCraneContainer1.getExchangeContainer());
        }
        if (!wiAreaAbilityList.contains(wiAreaAbility2)) {
            exCntList2 = findOptimalSingleExchangeCntList(wiCraneContainer2, wiAreaAbilityList, wiData);
        } else {
            WIContainer wiContainer = wiCraneContainer2.getExchangeContainer();
            wiContainer.setOverCntNumTemp(overCntNum2);
            exCntList2.add(wiContainer);
        }
        if (exCntList1.size() == 1) {
            for (WIContainer wiContainer2 : exCntList2) {
                if (exCntList1.get(0).getAreaNo().equals(wiContainer2.getAreaNo()) && !exCntList1.get(0).getyLocation().equals(wiContainer2.getyLocation())) {
                    wiContainerList.add(exCntList1.get(0));
                    wiContainerList.add(wiContainer2);
                    return wiContainerList;
                }
            }
        }
        if (exCntList2.size() == 1) {
            for (WIContainer wiContainer1 : exCntList1) {
                if (exCntList2.get(0).getAreaNo().equals(wiContainer1.getAreaNo()) && !exCntList2.get(0).getyLocation().equals(wiContainer1.getyLocation())) {
                    wiContainerList.add(wiContainer1);
                    wiContainerList.add(exCntList2.get(0));
                    return wiContainerList;
                }
            }
        }
        for (WIContainer wiContainer1 : exCntList1) {
            for (WIContainer wiContainer2 : exCntList2) {
                if (wiContainer1.getAreaNo().equals(wiContainer2.getAreaNo()) && !wiContainer1.getyLocation().equals(wiContainer2.getyLocation())) {
                    wiContainerList.add(wiContainer1);
                    wiContainerList.add(wiContainer2);
                    return wiContainerList;
                }
            }
        }
        return wiContainerList;
    }

    //------------------基础方法---------------

    private WICraneMove getFirstMove(Integer rowNo, Integer bayNo, Map<Integer, List<WICraneMove>> rowNoCraneMoveMap, String dlType) {
        List<WICraneMove> wiCraneMoveList = rowNoCraneMoveMap.get(rowNo);
        if (wiCraneMoveList != null && wiCraneMoveList.size() > 0) {
            if (CWPDomain.DL_TYPE_LOAD.equals(dlType)) {
                PublicMethod.sortWICraneMoveByTierNoAsc(wiCraneMoveList);
            }
            if (CWPDomain.DL_TYPE_DISC.equals(dlType)) {
                PublicMethod.sortWICraneMoveByTierNoDesc(wiCraneMoveList);
            }
            for (WICraneMove wiCraneMove : wiCraneMoveList) {
                if (wiCraneMove.getSentSeq() >= WIDefaultValue.sentSeq && bayNo.equals(wiCraneMove.getWorkBayNo())) {
                    return wiCraneMove;
                }
                if (wiCraneMove.getSentSeq() >= WIDefaultValue.sentSeq && bayNo % 2 == 0 && !bayNo.equals(wiCraneMove.getWorkBayNo())) {
                    break;
                }
            }
        }
        return null;
    }

    private List<WICraneMove> removeOtherMove(List<WICraneMove> curCanWorkMoves) {
        List<WICraneMove> newCurCanWorkMoves = new ArrayList<>();
        PublicMethod.sortWICraneMoveByMoveOrder(curCanWorkMoves);
        WICraneMove wiCraneMoveFirst = curCanWorkMoves.get(0);
        for (WICraneMove wiCraneMove : curCanWorkMoves) {
            if (wiCraneMoveFirst.getWorkflow().equals(wiCraneMove.getWorkflow())
                    && wiCraneMoveFirst.getWiCraneContainerList().size() == wiCraneMove.getWiCraneContainerList().size()) {
                newCurCanWorkMoves.add(wiCraneMove);
            }
        }
        return newCurCanWorkMoves;
    }

    private List<WICraneMove> limitTierHeight(List<WICraneMove> curCanWorkMoves, WIData wiData) {
        List<WICraneMove> newCurCanWorkMoves = new ArrayList<>();
        PublicMethod.sortWICraneMoveByTierNoAsc(curCanWorkMoves);
        int minTierNo = curCanWorkMoves.get(0).getTierNo();
        int tn = minTierNo > 50 ? wiData.getWiConfiguration().getDeckTierNum() : wiData.getWiConfiguration().getHatchTierNum();
        for (WICraneMove wiCraneMove : curCanWorkMoves) {
            if (wiCraneMove.getTierNo() - minTierNo < tn) {
                newCurCanWorkMoves.add(wiCraneMove);
            }
        }
        return newCurCanWorkMoves;
    }

    private boolean notExchangeCnt(WIContainer cnt) {
        //危险品、冷藏箱、超限箱
        boolean not1 = WIDomain.YES.equals(cnt.getDgCd()) || WIDomain.YES.equals(cnt.getRfFlag()) || WIDomain.YES.equals(cnt.getOverrunCd());
        return WIDomain.NO.equals(cnt.getCwoManualLocation()) || not1;
    }

    private boolean conformRules(WICraneContainer wiCraneContainer, WIContainer cnt, WIData wiData) {
        //交换范围规则
        WIConfiguration wiConfiguration = wiData.getWiConfiguration();
        String vLocation = wiCraneContainer.getvLocation();
        VMPosition vmPosition = new VMPosition(vLocation);
        VMPosition vmPositionEx = new VMPosition(cnt.getExchangeCraneContainer().getvLocation());
        boolean positionEx = false;
        if (WIDomain.YES.equals(wiConfiguration.getAllVesselExchange())) {
            positionEx = true;
        }
        if (WIDomain.YES.equals(wiConfiguration.getDeckAndHatchExchange())) {
            positionEx = true;
        }
        if (vmPosition.getTierNo() > 49) { //甲板上
            if (WIDomain.YES.equals(wiConfiguration.getDeckRowExchange()) && vmPositionEx.getTierNo() > 49 && vmPositionEx.getRowNo().equals(vmPosition.getRowNo())) {
                positionEx = true;
            }
            if (WIDomain.YES.equals(wiConfiguration.getDeckBayExchange()) && vmPositionEx.getTierNo() > 49) {
                positionEx = true;
            }
        } else { //甲板下
            if (WIDomain.YES.equals(wiConfiguration.getHatchRowExchange()) && vmPositionEx.getTierNo() < 49 && vmPositionEx.getRowNo().equals(vmPosition.getRowNo())) {
                positionEx = true;
            }
            if (WIDomain.YES.equals(wiConfiguration.getHatchBayExchange()) && vmPositionEx.getTierNo() < 49) {
                positionEx = true;
            }
        }
        if (WIDomain.YES.equals(wiCraneContainer.getExchangeContainer().getEfFlag()) && WIDomain.YES.equals(cnt.getEfFlag())) {
            if (WIDomain.YES.equals(wiConfiguration.getEmptyCntExchange())) { //允许空箱交换
                positionEx = true;
            }
        }
        //尺寸、箱型、目的港、高平
        if (!notExchangeCnt(cnt) && positionEx) {
            WIContainer wiContainer = wiCraneContainer.getExchangeContainer();
            if (wiContainer.getSize().equals(cnt.getSize())) {
                if (wiContainer.getType().equals(cnt.getType())) {
                    if (wiContainer.getDstPort().equals(cnt.getDstPort())) {
                        if (wiContainer.getIsHeight().equals(cnt.getIsHeight())) {
                            if (wiContainer.getWeightKg() != null && cnt.getWeightKg() != null) {
                                double weightD = Math.abs(wiContainer.getWeightKg() - cnt.getWeightKg());
                                if (hatchSideRowNo(wiCraneContainer.getHatchId(), vmPosition.getRowNo())) {
                                    return weightD <= wiData.getWiConfiguration().getHatchSideWeightDifference();
                                }
                                if (wiCraneContainer.getTierNo() > 50) {
                                    return weightD <= wiData.getWiConfiguration().getDeckWeightDifference();
                                } else {
                                    return weightD <= wiData.getWiConfiguration().getHatchWeightDifference();
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    private boolean hatchSideRowNo(Long hatchId, Integer rowNo) {
        VesselDataMethod vesselDataMethod = new VesselDataMethod(wiDataMethod.getVesselData());
        List<Integer> rowNoList = vesselDataMethod.getRowSeqListByHatchId(hatchId, CWPDomain.BOARD_ABOVE, CWPDomain.ROW_SEQ_ODD_EVEN);
        if (rowNoList.size() > 0) {
            return rowNoList.get(0).equals(rowNo) || rowNoList.get(rowNoList.size() - 1).equals(rowNo);
        }
        return false;
    }

    private int getOverCntNum(WIContainer cnt, List<WIContainer> wiContainerList) {
        List<WIContainer> wiContainers = new ArrayList<>();
        if (cnt != null) {
            for (WIContainer wiContainer : wiContainerList) {
                if (wiContainer.getyLocation() != null) {
                    String bayRowNo = wiContainer.getyLocation().substring(0, 5);
                    if (cnt.getyLocation().substring(0, 5).equals(bayRowNo)) {
                        wiContainers.add(wiContainer);
                    }
                }
            }
        }
        Collections.sort(wiContainers, new Comparator<WIContainer>() {
            @Override
            public int compare(WIContainer o1, WIContainer o2) {
                return o2.getyLocation().compareTo(o1.getyLocation());
            }
        });
        int count = 0;
        for (int i = 0; i < wiContainers.size(); i++) {
            WIContainer wiContainer = wiContainers.get(i);
            if (cnt.getyLocation().equals(wiContainer.getyLocation())) {
                count += i;
                break;
            }
            //去除已经发送了的箱子
            if (PublicMethod.isSentStatus(wiContainer.getWorkStatus())
                    || (wiContainer.getSentSeq() != null && wiContainer.getSentSeq() < WIDefaultValue.sentSeq)) {
                count -= 1;
            }
        }
        return count;
    }

    private void saveExchangeCntInfo(WICraneContainer wiCraneContainer, WIData wiData) {
        WIContainer originalCnt = wiCraneContainer.getOriginalContainerTemp();
        WIContainer exchangeCnt = wiCraneContainer.getExchangeContainer();
        if (!originalCnt.getyLocation().equals(exchangeCnt.getyLocation())) {
            if (!originalCnt.getAreaNo().equals(exchangeCnt.getAreaNo())) {
                WIAreaAbility cntAreaAbility = wiData.getWIAreaAbilityByAreaNo(originalCnt.getAreaNo());
                cntAreaAbility.setCurHourExchangeNum(cntAreaAbility.getCurHourExchangeNum() - 1);
                WIAreaAbility exCntAreaAbility = wiData.getWIAreaAbilityByAreaNo(exchangeCnt.getAreaNo());
                exCntAreaAbility.setCurHourExchangeNum(exCntAreaAbility.getCurHourExchangeNum() + 1);
            }

            WICraneContainer exCraneContainer = exchangeCnt.getExchangeCraneContainer();
            exCraneContainer.setOriginalContainerTemp(originalCnt);
            exCraneContainer.setExchangeContainer(originalCnt);
            originalCnt.setExchangeCraneContainer(exCraneContainer);
            exchangeCnt.setExchangeCraneContainer(wiCraneContainer);
        }
    }
}
