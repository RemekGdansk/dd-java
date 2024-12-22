package domaindrivers.smartschedule.planning.parallelization;


import java.time.Duration;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public record Stage(String stageName, Set<Stage> dependencies, Set<ResourceName> resources,
                    Duration duration, boolean financed) {

    public Stage(String name) {
        this(name, new HashSet<>(), new HashSet<>(), Duration.ZERO, false);
    }

    public Stage(String name, boolean financed) {
        this(name, new HashSet<>(), new HashSet<>(), Duration.ZERO, financed);
    }

    public Stage dependsOn(Stage stage) {
        this.dependencies.add(stage);
        return this;
    }

    boolean hasCircularDependency() {
        return hasDependencyTo(this);
    }

    boolean hasDependencyTo(Stage toFind) {
        if (this.dependencies().isEmpty()) {
            return false;
        } else if (this.dependencies().contains(toFind)) {
            return true;
        } else {
            return this.dependencies().stream().anyMatch(dependency -> dependency.hasDependencyTo(toFind));
        }
    }

    public String name() {
        return stageName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Stage stage = (Stage) o;
        return Objects.equals(stageName, stage.stageName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(stageName);
    }

    @Override
    public String toString() {
        return "Stage{" +
                "stageName='" + stageName + '\'' +
                ", dependencies=" + dependencies.stream().map(Stage::stageName) +
                ", resources=" + resources.stream().map(ResourceName::name) +
                ", duration=" + duration +
                '}';
    }
}

record ResourceName(String name) {

}
