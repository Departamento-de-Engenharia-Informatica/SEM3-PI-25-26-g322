package isep.ipp.pt.g322;
import isep.ipp.pt.g322.model.Allocation;
import isep.ipp.pt.g322.service.PickingPlanService;

import java.util.List;

public class Main {
    public static void main(String[] args) {

        // Cria o serviço e popula o unitWeight
        PickingPlanService pps = new PickingPlanService();
        pps.populateUnitWeight();

        // Pega as allocations já atualizadas
        List<Allocation> allocations = pps.getAllocations();

        // Imprime para conferir
        System.out.println("===== ALLOCATIONS COM UNIT WEIGHT =====");
        System.out.println("SKU\tQuantity\tUnitWeight\tTotalWeight");
        for (Allocation alloc : allocations) {
            System.out.println(alloc.getSku() + "\t"
                    + alloc.getQtAlloc() + "\t"
                    + alloc.getUnitWeight() + "\t"
                    + alloc.getTotalWeight());
        }

        System.out.println("\nTest completed: All allocations should now have unitWeight set.");
                // TODO: quando quisermos ligar a UI/texto, chamamos InventoryService/CsvImporter aqui.

    }
}

