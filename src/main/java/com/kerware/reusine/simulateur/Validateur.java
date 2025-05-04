package com.kerware.reusine.simulateur;

import com.kerware.simulateur.SituationFamiliale;

public class Validateur {

    public static void validiteParametresCalCulImpot(
            int revenuNetDeclarant1,
            int revenuNetDeclarant2,
            SituationFamiliale situationFamiliale,
            int nombreEnfants,
            int nombreEnfantsHandicapes,
            boolean parentIsole
    ) throws IllegalArgumentException {
        valeurNegative(revenuNetDeclarant1, "Le revenu net ne peut être négatif");
        valeurNegative(revenuNetDeclarant2, "Le revenu net ne peut être négatif");
        valeurNegative(nombreEnfants, "Le nombre d'enfants ne peut être négatif");
        valeurNegative(nombreEnfantsHandicapes, "Le nombre d'enfants handicapés ne peut être négatif");
        nombreEnfants(nombreEnfants, nombreEnfantsHandicapes);
        boolean personneSeule = estSeule(situationFamiliale);
        situationFamiliale(situationFamiliale, personneSeule, parentIsole, revenuNetDeclarant2);
    }

    private static boolean estSeule(SituationFamiliale situationFamiliale){
        return (situationFamiliale == SituationFamiliale.CELIBATAIRE || situationFamiliale == SituationFamiliale.DIVORCE || situationFamiliale == SituationFamiliale.VEUF);
    }

    private static void nombreEnfants(int nombreEnfants, int nombreEnfantsHandicapes){
        if (nombreEnfantsHandicapes > nombreEnfants) {
            throw new IllegalArgumentException("Le nombre d'enfants handicapés ne peut être supérieur au nombre d'enfants");
        }
        if (nombreEnfants > 7) {
            throw new IllegalArgumentException("Le nombre d'enfants ne peut être supérieur à 7");
        }
    }

    private static void valeurNegative(int valeur, String message) throws IllegalArgumentException{
        if(valeur < 0){
            throw new IllegalArgumentException(message);
        }
    }

    private static void situationFamiliale(
            SituationFamiliale situationFamiliale,
            boolean personneSeule,
            boolean parentIsole,
            int revenuNetDeclarant2
    ) throws IllegalArgumentException {
        if (situationFamiliale == null) {
            throw new IllegalArgumentException("La situation familiale doit être renseignée");
        }
        if (parentIsole && (situationFamiliale == SituationFamiliale.MARIE || situationFamiliale == SituationFamiliale.PACSE)) {
            throw new IllegalArgumentException("Un parent isolé ne peut être marié ou pacsé");
        }
        if (personneSeule && revenuNetDeclarant2 > 0) {
            throw new IllegalArgumentException("Un célibataire, un divorcé ou un veuf ne peut avoir de revenu pour le déclarant 2");
        }
    }
}
