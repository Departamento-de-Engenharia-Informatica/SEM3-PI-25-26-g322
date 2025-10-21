import isep.ipp.pt.g322.model.Allocation;
import isep.ipp.pt.g322.model.Trolley;
import isep.ipp.pt.g322.service.PickingPlanService;
import isep.ipp.pt.g322.service.TrolleyAllocatorService;
import isep.ipp.pt.g322.service.TrolleyAllocatorService.Heuristic;

import java.util.List;
import java.util.Scanner;

public class TrolleyTest {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        double capacity;
        int choice;
        Heuristic heuristicChoice = Heuristic.FF; // valor padrão

        PickingPlanService pps = new PickingPlanService();
        pps.populateUnitWeight();
        List<Allocation> allocations = pps.getAllocations();

        TrolleyAllocatorService service = new TrolleyAllocatorService();

        System.out.print("Please insert trolley capacity: ");
        capacity = scanner.nextDouble();

        System.out.println("Please choose the heuristic: ");
        System.out.println("1 - FF  (First Fit)");
        System.out.println("2 - FFD (First Fit Decreasing)");
        System.out.println("3 - BFD (Best Fit Decreasing)");
        System.out.print("Enter your choice: ");

        choice = scanner.nextInt();

        switch (choice) {
            case 1:
                heuristicChoice = Heuristic.FF;
                break;
            case 2:
                heuristicChoice = Heuristic.FFD;
                break;
            case 3:
                heuristicChoice = Heuristic.BFD;
                break;
            default:
                System.out.println("Invalid choice. Defaulting to FF.");
                heuristicChoice = Heuristic.FF;
                break;
        }

        List<Trolley> trolleys = service.allocateTrolleys(allocations, capacity, heuristicChoice);

        System.out.println("\n===== PICKING PLAN (" + heuristicChoice + ") =====");
        pps.printPickingPlan(trolleys);
    }
}
