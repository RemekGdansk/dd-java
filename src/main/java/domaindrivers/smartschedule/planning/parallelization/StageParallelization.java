package domaindrivers.smartschedule.planning.parallelization;

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
            final List<Set<Stage>> financedAndNotFinanced =
                    stagesForNextParallelStages.stream().collect(Collectors.teeing(
                            Collectors.filtering(Stage::financed, Collectors.toSet()),
                            Collectors.filtering(stage -> !stage.financed(), Collectors.toSet()),
                            List::of
                    ));
            for (Set<Stage> stagesByFinancing : financedAndNotFinanced) {
                if (!stagesByFinancing.isEmpty()) {
                    parallelStagesList = parallelStagesList.add(new ParallelStages(stagesByFinancing));
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

    boolean hasUnfulfilledDependency(Stage toAnalyze, Set<Stage> fulfilledStages) {
        return toAnalyze.dependencies().stream().anyMatch(dependency -> !fulfilledStages.contains(dependency));
    }

}
