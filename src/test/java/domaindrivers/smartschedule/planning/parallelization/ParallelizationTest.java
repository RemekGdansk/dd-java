package domaindrivers.smartschedule.planning.parallelization;

import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ParallelizationTest {

    static final StageParallelization stageParallelization = new StageParallelization();

    @Test
    void everythingCanBeDoneInParallelWhenThereAreNoDependencies() {
        //given
        Stage stage1 = new Stage("Stage1");
        Stage stage2 = new Stage("Stage2");

        //when
        ParallelStagesList sortedStages = stageParallelization.of(Set.of(stage1, stage2));

        //then
        assertEquals(1, sortedStages.all().size());
    }

    @Test
    void testSimpleDependencies() {
        //given
        Stage stage1 = new Stage("Stage1");
        Stage stage2 = new Stage("Stage2");
        Stage stage3 = new Stage("Stage3");
        Stage stage4 = new Stage("Stage4");
        stage2.dependsOn(stage1);
        stage3.dependsOn(stage1);
        stage4.dependsOn(stage2);

        //when
        ParallelStagesList sortedStages = stageParallelization.of(Set.of(stage1, stage2, stage3, stage4));

        //then
        assertEquals("Stage1 | Stage2, Stage3 | Stage4", sortedStages.print());
    }

    @Test
    void financedShouldBeBeforeNotFinanced() {
        //given
        Stage stage1 = new Stage("Stage1");
        Stage stage2 = new Stage("Stage2", true);
        Stage stage3 = new Stage("Stage3");
        Stage stage4 = new Stage("Stage4", true);
        stage2.dependsOn(stage1);
        stage3.dependsOn(stage1);
        stage4.dependsOn(stage1);

        //when
        ParallelStagesList sortedStages = stageParallelization.of(Set.of(stage1, stage2, stage3, stage4));

        //then
        assertEquals("Stage1 | Stage2, Stage4 | Stage3", sortedStages.print());
    }

    @Test
    void sameResourceCannotBeInParallelStages() {
        //given
        Stage stage1 = new Stage("Stage1");
        Stage stage2 = new Stage("Stage2");
        Stage stage3 = new Stage("Stage3");
        ResourceName resourceA = new ResourceName("resourceA");
        ResourceName resourceB = new ResourceName("resourceB");
        stage1.requiresResource(resourceA);
        stage1.requiresResource(resourceB);
        stage2.requiresResource(resourceA);
        stage3.requiresResource(resourceB);

        //when
        ParallelStagesList sortedStages = stageParallelization.of(Set.of(stage1, stage2, stage3));

        //then
        assertEquals("Stage1 | Stage2, Stage3", sortedStages.print());
    }

    @Test
    void testDependenciesFinancingAndResources() {
        //given
        Stage stage1 = new Stage("Stage1");
        Stage stage2 = new Stage("Stage2");
        Stage stage3 = new Stage("Stage3");
        Stage stage4 = new Stage("Stage4", true);
        Stage stage5 = new Stage("Stage5");
        Stage stage6 = new Stage("Stage6", true);
        Stage stage7 = new Stage("Stage7");

        stage2.dependsOn(stage1);
        stage3.dependsOn(stage1);
        stage4.dependsOn(stage2);
        stage5.dependsOn(stage2);
        stage6.dependsOn(stage3);
        stage7.dependsOn(stage3);

        ResourceName resourceA = new ResourceName("resourceA");

        stage4.requiresResource(resourceA);
        stage5.requiresResource(resourceA);
        stage6.requiresResource(resourceA);

        //when
        ParallelStagesList sortedStages = stageParallelization.of(Set.of(stage1, stage2, stage3, stage4, stage5, stage6, stage7));

        //then
        assertEquals("Stage1 | Stage2, Stage3 | Stage4 | Stage6 | Stage5, Stage7", sortedStages.print());
    }

    @Test
    void cantBeDoneWhenThereIsACycle() {
        //given
        Stage stage1 = new Stage("Stage1");
        Stage stage2 = new Stage("Stage2");
        stage2.dependsOn(stage1);
        stage1.dependsOn(stage2); // making it cyclic

        //when
        ParallelStagesList sortedStages = stageParallelization.of(Set.of(stage1, stage2));

        //then
        assertTrue(sortedStages.all().isEmpty());
    }

}
