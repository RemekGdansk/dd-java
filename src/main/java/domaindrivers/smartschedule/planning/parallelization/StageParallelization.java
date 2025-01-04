package domaindrivers.smartschedule.planning.parallelization;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;


public class StageParallelization {

    public ParallelStagesList of(Set<Stage> stages) {
        ParallelStagesList parallelStagesList = ParallelStagesList.empty();
        if (stages.stream().anyMatch(Stage::hasCircularDependency)) {
            return parallelStagesList;
        }
        final Set<Stage> remainingStages = new HashSet<>(stages);
        while (!remainingStages.isEmpty()) {
            // Divide by logical dependencies
            final ParallelStagesList finalParallelStagesList = parallelStagesList;
            final Set<Stage> stagesForNextParallelStages = remainingStages.stream()
                    .filter(stage -> !hasUnfulfilledDependency(stage, finalParallelStagesList.allStages()))
                    .collect(Collectors.toSet());
            // Divide into financed and not financed
            final List<List<Stage>> financedAndNotFinanced =
                    stagesForNextParallelStages.stream().collect(Collectors.teeing(
                            Collectors.filtering(Stage::financed, Collectors.toList()),
                            Collectors.filtering(stage -> !stage.financed(), Collectors.toList()),
                            List::of
                    ));
            for (List<Stage> stagesByFinancing : financedAndNotFinanced) {
                if (!stagesByFinancing.isEmpty()) {
                    final List<Set<Stage>> setsByFinancingAndNoResourceInParallel =
                            stagesInSetsHavingGivenResourceAtMostOnce(stagesByFinancing);
                    for (Set<Stage> setByFinancingAndNoResourceInParallel : setsByFinancingAndNoResourceInParallel) {
                        parallelStagesList = parallelStagesList.add(new ParallelStages(setByFinancingAndNoResourceInParallel));
                    }
                }
            }
            // Remove already assigned stages from further analysis
            final Iterator<Stage> i = remainingStages.iterator();
            while (i.hasNext()) {
                final Stage stage = i.next();
                if (parallelStagesList.contains(stage)) {
                    i.remove();
                }
            }
        }
        System.out.println("Debug: " + parallelStagesList.print());
        return parallelStagesList;
    }

    private List<Set<Stage>> stagesInSetsHavingGivenResourceAtMostOnce(List<Stage> stages) {
        final List<Stage> remainingStages =
                new ArrayList<>(stages.stream().sorted(Comparator.comparing(Stage::name)).toList());
        final List<Set<Stage>> setsHavingGivenResourceAtMostOnce = new ArrayList<>();
        final Iterator<Stage> i = remainingStages.iterator();
        while (i.hasNext()) {
            final Stage stage = i.next();
            if (setsHavingGivenResourceAtMostOnce.isEmpty()) {
                final Set<Stage> setHavingGivenResourceAtMostOnce = new HashSet<>();
                setHavingGivenResourceAtMostOnce.add(stage);
                setsHavingGivenResourceAtMostOnce.add(setHavingGivenResourceAtMostOnce);
            } else {
                boolean stageAdded = false;
                for (Set<Stage> currentSet : setsHavingGivenResourceAtMostOnce) {
                    final Set<ResourceName> resourcesInSet = currentSet.stream()
                            .map(Stage::resources)
                            .flatMap(Set::stream)
                            .collect(Collectors.toSet());
                    final boolean canAddResource = Collections.disjoint(stage.resources(), resourcesInSet);
                    if (canAddResource) {
                        currentSet.add(stage);
                        stageAdded = true;
                    }
                }
                if (!stageAdded) {
                    final Set<Stage> newStages = new HashSet<>();
                    newStages.add(stage);
                    setsHavingGivenResourceAtMostOnce.add(newStages);
                }
            }
            i.remove();
        }
        return setsHavingGivenResourceAtMostOnce;
    }

    private boolean hasUnfulfilledDependency(Stage toAnalyze, Set<Stage> fulfilledStages) {
        return toAnalyze.dependencies().stream().anyMatch(dependency -> !fulfilledStages.contains(dependency));
    }

}
