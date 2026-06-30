package tcc.bes.api_monolito.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "tcc.bes.api_monolito")
class ModularArchitectureTest {

    @ArchTest
    static final ArchRule domainDoesNotDependOnAdapters = noClasses()
            .that().resideInAPackage("..domain..")
            .should().dependOnClassesThat().resideInAnyPackage("..interfaces..", "..infrastructure..");

    @ArchTest
    static final ArchRule identityDoesNotDependOnBusinessModules = noClasses()
            .that().resideInAPackage("..identity..")
            .should().dependOnClassesThat().resideInAnyPackage(
                    "..queuemanagement..",
                    "..reservation..",
                    "..waitingroom.."
            );

    @ArchTest
    static final ArchRule reservationCoreDoesNotDependOnQueueOrWaitingRoom = noClasses()
            .that().resideInAnyPackage("..reservation.domain..", "..reservation.application..")
            .should().dependOnClassesThat().resideInAnyPackage("..queuemanagement..", "..waitingroom..");

    @ArchTest
    static final ArchRule waitingRoomDoesNotDependOnQueueApplicationService = noClasses()
            .that().resideInAPackage("..waitingroom.application..")
            .should().dependOnClassesThat()
            .haveFullyQualifiedName("tcc.bes.api_monolito.queuemanagement.application.QueueApplicationService");

    @ArchTest
    static final ArchRule waitingRoomDoesNotDependOnReservationApplicationService = noClasses()
            .that().resideInAPackage("..waitingroom.application..")
            .should().dependOnClassesThat()
            .haveFullyQualifiedName("tcc.bes.api_monolito.reservation.application.ReservationApplicationService");
}
