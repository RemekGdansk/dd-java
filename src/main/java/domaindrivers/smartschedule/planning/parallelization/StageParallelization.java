package domaindrivers.smartschedule.planning.parallelization;

import java.util.HashSet;
import java.util.Iterator;
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
            final ParallelStagesList finalParallelStagesList = parallelStagesList;
            final Set<Stage> stagesForNextParallelStages = remainingStages.stream()
                    .filter(stage -> !hasUnfulfilledDependency(stage, finalParallelStagesList.allStages()))
                    .collect(Collectors.toSet());
            parallelStagesList = parallelStagesList.add(new ParallelStages(stagesForNextParallelStages));
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

    boolean hasUnfulfilledDependency(Stage toAnalyze, Set<Stage> fulfilledStages) {
        return toAnalyze.dependencies().stream().anyMatch(dependency -> !fulfilledStages.contains(dependency));
    }

}
