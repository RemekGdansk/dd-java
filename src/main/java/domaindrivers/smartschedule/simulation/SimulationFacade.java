package domaindrivers.smartschedule.simulation;

import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;


public class SimulationFacade {

    public Result whichProjectWithMissingDemandsIsMostProfitableToAllocateResourcesTo(
            final List<SimulatedProject> projects,
            final SimulatedCapabilities totalCapability
    ) {
        final List<List<SimulatedProject>> allProjectCombinations = allProjectCombinations(projects);
        final List<List<SimulatedProject>> possibleProjectCombinations =
                possibleProjectCombinations(allProjectCombinations, totalCapability);
        final Map<List<SimulatedProject>, BigDecimal> projectCombinationsWithProfits =
                projectCombinationsWithProfits(possibleProjectCombinations);
        final Optional<Map.Entry<List<SimulatedProject>, BigDecimal>> mostProfitableCombinationOptional =
                projectCombinationsWithProfits.entrySet().stream().max(Map.Entry.comparingByValue());
        if (mostProfitableCombinationOptional.isEmpty()) {
            return new Result(0d, List.of(), Map.of());
        }
        final Map.Entry<List<SimulatedProject>, BigDecimal> mostProfitableCombination =
                mostProfitableCombinationOptional.get();
        // Saving resources allocation not implemented
        final Map<SimulatedProject, Set<AvailableResourceCapability>> resourcesAllocatedToProjects = new HashMap<>();
        return new Result(
                mostProfitableCombination.getValue().doubleValue(),
                mostProfitableCombination.getKey(),
                resourcesAllocatedToProjects
        );
    }

    private List<List<SimulatedProject>> allProjectCombinations(final List<SimulatedProject> projects) {
        final List<List<SimulatedProject>> result = new ArrayList<>();
        generateProjectCombinations(List.copyOf(projects), 0, new ArrayList<>(), result);
        return result;
    }

    private void generateProjectCombinations(
            final List<SimulatedProject> inputProjects,
            final int index,
            final List<SimulatedProject> currentSubset,
            final List<List<SimulatedProject>> result) {
        result.add(currentSubset);
        for (int i = index; i < inputProjects.size(); i++) {
            currentSubset.add(inputProjects.get(i)); // Include the current project
            generateProjectCombinations(inputProjects, i + 1, new ArrayList<>(currentSubset), result); // Add next project
            currentSubset.remove(currentSubset.size() - 1); // Exclude the current project from next iteration
        }
    }

    private List<List<SimulatedProject>> possibleProjectCombinations(
            final List<List<SimulatedProject>> projectCombinations,
            final SimulatedCapabilities totalCapability
    ) {
        return projectCombinations.stream()
                .filter(pc -> projectCombinationPossible(pc, totalCapability))
                .toList();
    }

    private boolean projectCombinationPossible(
            final List<SimulatedProject> projectCombination,
            final SimulatedCapabilities totalCapability
    ) {
        final List<AvailableResourceCapability> unallocatedCapabilities =
                new ArrayList<>(totalCapability.capabilities());
        for (SimulatedProject project : projectCombination) {
            for (Demand demand : project.missingDemands().all()) {
                final Optional<AvailableResourceCapability> unallocatedCapability = unallocatedCapabilities.stream()
                        .filter(uc -> uc.performs(demand.capability()))
                        .findAny();
                if (unallocatedCapability.isEmpty()) {
                    return false;
                } else {
                    unallocatedCapabilities.remove(unallocatedCapability.get());
                }
            }
        }
        return true;
    }


    private Map<List<SimulatedProject>, BigDecimal> projectCombinationsWithProfits(
            final List<List<SimulatedProject>> projectCombinations
    ) {
        return projectCombinations.stream()
                .map(pc -> Map.entry(pc, calculateProfit(pc)))
                .sorted((e1, e2) -> e2.getValue().compareTo(e1.getValue()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new)
                );
    }

    private BigDecimal calculateProfit(final List<SimulatedProject> projects) {
        return projects.stream()
                .map(SimulatedProject::earnings)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private boolean demandsFulfilled(final List<SimulatedProject> projects, final SimulatedCapabilities capabilities) {
        List<AvailableResourceCapability> unallocatedCapabilities = List.copyOf(capabilities.capabilities());
        return false;
    }







}

