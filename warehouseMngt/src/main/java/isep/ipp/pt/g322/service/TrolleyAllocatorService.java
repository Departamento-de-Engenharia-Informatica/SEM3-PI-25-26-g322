package isep.ipp.pt.g322.service;

import isep.ipp.pt.g322.model.Allocation;
import isep.ipp.pt.g322.model.AllocationFragment;
import isep.ipp.pt.g322.model.Trolley;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TrolleyAllocatorService {

    public enum Heuristic {FF, FFD, BFD}

    public List<Trolley> allocateTrolleys(List<Allocation> allocations, double trolleyCapacity, Heuristic heuristic) {
        List<Trolley> trolleys = new ArrayList<>();
        int trolleyIdCounter = 1;

        switch (heuristic) {
            case FFD:
            case BFD:
                allocations.sort(Comparator.comparingDouble(Allocation::getTotalWeight).reversed());
                break;
            case FF:
                break;
        }
        //Itera por todas as alocações ( Se FFD ou BFD, já estão ordenadas por peso decrescente )
        for (Allocation allocation : allocations) {
            double remainingQty = allocation.getQtAlloc();
            double unitWeight = allocation.getUnitWeight();
            //Dividimos a alocação em fragmentos que cabem nos trolleys
            while (remainingQty > 0) {
                Trolley targetTrolley = null;
                double qtyThatFits = 0;

                if (heuristic == Heuristic.BFD) {
                    // variável que guarda o menor espaço restante encontrado
                    double minRemaining = Double.MAX_VALUE;
                    for (Trolley trolley : trolleys) {
                        double canFitQty = Math.floor((trolley.getCapacity() - trolley.getUsedWeight()) / unitWeight);
                        if (canFitQty > 0) {
                            double remainingAfter = (trolley.getCapacity() - trolley.getUsedWeight()) - canFitQty * unitWeight;
                            //Verifica se este trolley tem menos espaço desperdiçado que o melhor até agora
                            if (remainingAfter < minRemaining) {
                                minRemaining = remainingAfter;
                                targetTrolley = trolley;
                                qtyThatFits = Math.min(remainingQty, canFitQty);
                            }
                        }
                    }
                } else {
                    for (Trolley trolley : trolleys) {
                        double canFitQty = Math.floor((trolley.getCapacity() - trolley.getUsedWeight()) / unitWeight);
                        if (canFitQty > 0) {
                            targetTrolley = trolley;
                            qtyThatFits = Math.min(remainingQty, canFitQty);
                            // First Fit - sai do loop ao encontrar o primeiro trolley que cabe
                            break;
                        }   
                    }
                }

                if (targetTrolley == null) {
                    trolleyIdCounter++;
                    targetTrolley = new Trolley(trolleyIdCounter, trolleyCapacity);
                    trolleys.add(targetTrolley);
                    qtyThatFits = Math.min(remainingQty, Math.floor(trolleyCapacity / unitWeight));
                }
                // Cria um fragmento de alocação para a quantidade que foi colocada no trolley
                AllocationFragment frag = new AllocationFragment();
                frag.setOriginalAllocation(allocation);
                frag.setOrderID(allocation.getOrderID());
                frag.setLineNumber(allocation.getLineNumber());
                frag.setSku(allocation.getSku());
                frag.setQtyPlaced((int) qtyThatFits);
                frag.setWeightPlaced(qtyThatFits * unitWeight);
                frag.setTrolleyId(targetTrolley.getTrolleyID());
                frag.setBoxID(allocation.getBoxID());
                frag.setAisle(allocation.getAisle());
                frag.setBay(allocation.getBay());

                targetTrolley.addFragment(frag);

                remainingQty -= qtyThatFits;
            }
        }

        return trolleys;
    }

}
