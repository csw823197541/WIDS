package com.wids.service.method;

import com.wids.model.domain.CWPCntDomain;
import com.wids.model.domain.CWPDomain;
import com.wids.model.domain.WIDomain;
import com.wids.model.wi.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * Created by csw on 2017/10/12.
 * Description:
 */
public class PublicMethod {

    public static boolean isUnSentStatus(String workStatus) {
        return WIDomain.Y.equals(workStatus) || WIDomain.S.equals(workStatus) || WIDomain.P.equals(workStatus);
    }

    public static boolean canNotLoadState(String workStatus) {
        return WIDomain.P.equals(workStatus);
    }

    public static boolean canLoadState(String workStatus) {
        return WIDomain.S.equals(workStatus);
    }

    public static boolean isSentStatus(String workStatus) {
        return WIDomain.A.equals(workStatus) || WIDomain.W.equals(workStatus);
    }

    public static boolean isWorkDoneStatus(String workStatus) {
        return WIDomain.C.equals(workStatus) || WIDomain.RC.equals(workStatus);
    }

    public static boolean isSentToWorkBlock(String workStatus) {
        return WIDomain.SendToWork.equals(workStatus) || WIDomain.Work.equals(workStatus);
    }

    public static boolean isPreWorkBlock(String workStatus) {
        return WIDomain.Pre.equals(workStatus);
    }

    public static boolean isSentOrPreToWorkBlock(String workStatus) {
        return WIDomain.SendToWork.equals(workStatus) || WIDomain.Work.equals(workStatus) || WIDomain.Pre.equals(workStatus) || WIDomain.SubmitToWork.equals(workStatus);
    }

    public static boolean specialArea(String key) {
        return key.startsWith("C") || key.startsWith("T") || key.startsWith("W");
    }

    public static boolean areaMoveStage(String moveStage) {
        return CWPCntDomain.YARD.equals(moveStage);
    }

    /**
     * 船舶左靠，反向靠泊（奇数排靠近海侧）;船舶右靠，正向靠泊（偶数排靠近海侧），得到奇数排号开始，还是偶数排号
     *
     * @param seaOrLand 从海侧开始，还是从陆侧开始
     * @return
     */
    public static String getOddOrEvenBySeaOrLand(String planBerthDirect, String seaOrLand) {
        boolean sl = seaOrLand.equals(CWPDomain.ROW_SEQ_SEA_LAND);
        String oe = sl ? CWPDomain.ROW_SEQ_ODD_EVEN : CWPDomain.ROW_SEQ_EVEN_ODD;
        String eo = sl ? CWPDomain.ROW_SEQ_EVEN_ODD : CWPDomain.ROW_SEQ_ODD_EVEN;
        return planBerthDirect.equals(CWPDomain.VES_BER_DIRECT_L) ? oe : eo;
    }

    public static void sortWICraneContainerByMoveOrder(List<WICraneContainer> wiCraneContainerList) {
        Collections.sort(wiCraneContainerList, new Comparator<WICraneContainer>() {
            @Override
            public int compare(WICraneContainer o1, WICraneContainer o2) {
                if (o1.getBayNo().equals(o2.getBayNo())) {
                    return o1.getMoveOrder().compareTo(o2.getMoveOrder());
                } else {
                    return o1.getBayNo().compareTo(o2.getBayNo());
                }
            }
        });
    }

    public static void sortWICraneByCraneSeq(List<WICrane> wiCraneList) {
        Collections.sort(wiCraneList, new Comparator<WICrane>() {
            @Override
            public int compare(WICrane o1, WICrane o2) {
                return o1.getCraneSeq().compareTo(o2.getCraneSeq());
            }
        });
    }

    public static void sortWIWorkBlockByCraneSeq(List<WIWorkBlock> wiWorkBlockList) {
        Collections.sort(wiWorkBlockList, new Comparator<WIWorkBlock>() {
            @Override
            public int compare(WIWorkBlock o1, WIWorkBlock o2) {
                return o1.getCraneSeq().compareTo(o2.getCraneSeq());
            }
        });
    }

    public static void sortWIWorkBlockByHatchSeq(List<WIWorkBlock> wiWorkBlockList) {
        Collections.sort(wiWorkBlockList, new Comparator<WIWorkBlock>() {
            @Override
            public int compare(WIWorkBlock o1, WIWorkBlock o2) {
                return o1.getHatchSeq().compareTo(o2.getHatchSeq());
            }
        });
    }

    public static void sortWICraneMoveByMoveOrder(List<WICraneMove> wiCraneMoveList) {
        Collections.sort(wiCraneMoveList, new Comparator<WICraneMove>() {
            @Override
            public int compare(WICraneMove o1, WICraneMove o2) {
                return o1.getMoveOrder().compareTo(o2.getMoveOrder());
            }
        });
    }

    public static void sortWICraneMoveByStartTime(List<WICraneMove> wiCraneMoveList) {
        Collections.sort(wiCraneMoveList, new Comparator<WICraneMove>() {
            @Override
            public int compare(WICraneMove o1, WICraneMove o2) {
                return o1.getWorkingStartTime().compareTo(o2.getWorkingStartTime());
            }
        });
    }

    public static void sortWIWorkBlockByEstimateStartTime(List<WIWorkBlock> wiWorkBlockList) {
        Collections.sort(wiWorkBlockList, new Comparator<WIWorkBlock>() {
            @Override
            public int compare(WIWorkBlock o1, WIWorkBlock o2) {
                return o1.getEstimateStartTime().compareTo(o2.getEstimateStartTime());
            }
        });
    }

    public static boolean isSelected(WICraneMove wiCraneMove, List<WICraneMove> wiCraneMoveList) {
        for (WICraneMove wiCraneMove1 : wiCraneMoveList) {
            return wiCraneMove.getWorkBayKey().equals(wiCraneMove1.getWorkBayKey());
        }
        return false;
    }

    public static void sortWICraneMoveByTierNoAsc(List<WICraneMove> wiCraneMoveList) {
        Collections.sort(wiCraneMoveList, new Comparator<WICraneMove>() {
            @Override
            public int compare(WICraneMove o1, WICraneMove o2) {
                return o1.getTierNo().compareTo(o2.getTierNo());
            }
        });
    }

    public static void sortWICraneMoveByTierNoDesc(List<WICraneMove> wiCraneMoveList) {
        Collections.sort(wiCraneMoveList, new Comparator<WICraneMove>() {
            @Override
            public int compare(WICraneMove o1, WICraneMove o2) {
                return o2.getTierNo().compareTo(o1.getTierNo());
            }
        });
    }

    public static void sortWICraneMoveBySentSeq(List<WICraneMove> wiCraneMoveList) {
        Collections.sort(wiCraneMoveList, new Comparator<WICraneMove>() {
            @Override
            public int compare(WICraneMove o1, WICraneMove o2) {
                return o1.getSentSeq().compareTo(o2.getSentSeq());
            }
        });
    }

    public static void sortWIContainerByWorkingStartTime(List<WIContainer> wiContainerList) {
        Collections.sort(wiContainerList, new Comparator<WIContainer>() {
            @Override
            public int compare(WIContainer o1, WIContainer o2) {
                return o2.getWorkingStartTime().compareTo(o1.getWorkingStartTime());
            }
        });
    }
}
