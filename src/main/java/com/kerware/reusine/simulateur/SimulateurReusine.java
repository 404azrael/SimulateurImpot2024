package com.kerware.reusine.simulateur;


import com.kerware.simulateur.SituationFamiliale;

public class SimulateurReusine {
    private final int[] limitesTranchesRevenusImposables = {0, 11294, 28797, 82341, 177106, Integer.MAX_VALUE};

    private final double[] tauxImpositionParTranche = {0.0, 0.11, 0.3, 0.41, 0.45};

    private final int[] limitesTranchesCEHR = {0, 250000, 500000, 1000000, Integer.MAX_VALUE};

    //Taux de contribution exceptionnelle sur les hauts revenus (celibataires)
    private final double[] tauxCEHRCelibataire = {0.0, 0.03, 0.04, 0.04};

    //Taux de contribution exceptionnelle sur les hauts revenus (couples)
    private final double[] tauxCEHRCouple = {0.0, 0.0, 0.03, 0.04};


    // revenu net
    private int revenuNetDeclarant1;
    private int revenuNetDeclarant2;
    // nombre d'enfants
    private int nombreEnfants;
    // nombre d'enfants handicapés
    private int nombreEnfantsHandicapes;

    // revenu fiscal de référence
    private double revenuFiscalReference;

    // revenu imposable
    private double revenuImposable;

    // abattement
    private double abattement;

    // nombre de parts des déclarants
    private double nombrePartsDeclarant;
    // nombre de parts du foyer fiscal
    private double nombrePartsFoyerFiscal;

    // decote
    private double decote;
    // impôt des déclarants
    private double impotDeclarant;
    // impôt du foyer fiscal
    private double impotFoyerFiscal;
    //impôt avant decote
    private double impotAvantDecote;
    // parent isolé
    private boolean parentIsole;
    // Contribution exceptionnelle sur les hauts revenus
    private double contributionExceptionnelleHautsRevenus;

    // Getters pour adapter le code legacy pour les tests unitaires



    public double getRevenuReference() {
        return revenuFiscalReference;
    }

    public double getDecote() {
        return decote;
    }

    public double getAbattement() {
        return abattement;
    }

    public double getNbParts() {
        return nombrePartsFoyerFiscal;
    }

    public double getImpotAvantDecote() {
        return impotAvantDecote;
    }

    public double getImpotNet() {
        return impotFoyerFiscal;
    }

    public int getRevenuNetDeclatant1() {
        return revenuNetDeclarant1;
    }

    public int getRevenuNetDeclatant2() {
        return revenuNetDeclarant2;
    }

    public double getContribExceptionnelle() {
        return contributionExceptionnelleHautsRevenus;
    }

    //Initialisation des variables pour le calcul
    public void definirParametresCalculImpot(
            int revenuNetDeclarant1,
            int revenuNetDeclarant2,
            SituationFamiliale situationFamiliale,
            int nombreEnfants,
            int nombreEnfantsHandicapes,
            boolean parentIsole
    ) {
        this.revenuNetDeclarant1 = revenuNetDeclarant1;
        this.revenuNetDeclarant2 = revenuNetDeclarant2;

        this.nombreEnfants = nombreEnfants;
        this.nombreEnfantsHandicapes = nombreEnfantsHandicapes;
        this.parentIsole = parentIsole;

        System.out.println("--------------------------------------------------");
        System.out.println( "Revenu net declarant1 : " + this.revenuNetDeclarant1);
        System.out.println( "Revenu net declarant2 : " + this.revenuNetDeclarant2);
        System.out.println( "Situation familiale : " + situationFamiliale.name() );
    }


    //Calcul de l'impôt sur le revenu net en France en 2024 sur les revenus de 2023
    public int calculImpot(
            int revenuNetDeclarant1,
            int revenuNetDeclarant2,
            SituationFamiliale situationFamiliale,
            int nombreEnfants,
            int nombreEnfantsHandicapes,
            boolean parentIsole
    ) {

        try{
            //Préconditions
            //Validateur.validiteParametresCalCulImpot(revenuNetDeclarant1, revenuNetDeclarant2, situationFamiliale, nombreEnfants, nombreEnfantsHandicapes, parentIsole);

            //Initialisation des variables pour le calcul
            definirParametresCalculImpot(revenuNetDeclarant1, revenuNetDeclarant2, situationFamiliale, nombreEnfants, nombreEnfantsHandicapes, parentIsole);

            //Abattement
            //EXIGENCE : EXG_IMPOT_02
            calculAbattement(situationFamiliale);

            //Parts des déclarants
            //EXIG  : EXG_IMPOT_03
            calculPartsDeclarants(situationFamiliale);

            //EXIGENCE : EXG_IMPOT_07:
            //Contribution exceptionnelle sur les hauts revenus
            calculContributionsExceptionnellesHautsRevenus();

            //Calcul impôt des declarants
            //EXIGENCE : EXG_IMPOT_04
            calCulImpotDeclarant();

            //Calcul impôt foyer fiscal complet
            //EXIGENCE : EXG_IMPOT_04
            calculImpotFoyerFiscalComplet();

            //Vérification baisse d'impôt autorisée
            //EXIGENCE : EXG_IMPOT_05
            verificationBaisseImpot();

            //Calcul decote
            //EXIGENCE : EXG_IMPOT_06
            calculDecote();

            //Valeur de l'impôt renvoyée
            return (int) impotFoyerFiscal;
        }
        catch (Exception exception){
            System.out.println(exception.getMessage());
            throw exception;
        }
    }

    private void calculAbattement(SituationFamiliale situationFamiliale){
        double tauxAbattement = 0.1;
        int limiteAbattementMin = 495;
        int limiteAbattementMax = 14171;
        long abt1 = Math.round(revenuNetDeclarant1 * tauxAbattement);
        long abt2 = Math.round(revenuNetDeclarant2 * tauxAbattement);

        if (abt1 > limiteAbattementMax) {
            abt1 = limiteAbattementMax;
        }
        if (situationFamiliale == SituationFamiliale.MARIE || situationFamiliale == SituationFamiliale.PACSE) {
            if (abt2 > limiteAbattementMax) {
                abt2 = limiteAbattementMax;
            }
        }

        if (abt1 < limiteAbattementMin) {
            abt1 = limiteAbattementMin;
        }

        if (situationFamiliale == SituationFamiliale.MARIE || situationFamiliale == SituationFamiliale.PACSE) {
            if (abt2 < limiteAbattementMin) {
                abt2 = limiteAbattementMin;
            }
        }

        abattement = abt1 + abt2;
        System.out.println( "Abattement : " + abattement);

        revenuFiscalReference = revenuNetDeclarant1 + revenuNetDeclarant2 - abattement;
        if (revenuFiscalReference < 0) {
            revenuFiscalReference = 0;
        }

        System.out.println( "Revenu fiscal de référence : " + revenuFiscalReference);
    }

    public void calculPartsDeclarants(SituationFamiliale situationFamiliale){
        switch ( situationFamiliale ) {
            case CELIBATAIRE, DIVORCE, VEUF:
                nombrePartsDeclarant = 1;
                break;
            case MARIE, PACSE:
                nombrePartsDeclarant = 2;
                break;
            default:
                break;
        }

        System.out.println( "Nombre d'enfants  : " + this.nombreEnfants);
        System.out.println( "Nombre d'enfants handicapés : " + this.nombreEnfantsHandicapes);

        //Parts pour les enfants à charge
        if ( this.nombreEnfants <= 2 ) {
            nombrePartsFoyerFiscal = nombrePartsDeclarant + this.nombreEnfants * 0.5;
        } else {
            nombrePartsFoyerFiscal = nombrePartsDeclarant +  1.0 + ( this.nombreEnfants - 2 );
        }

        //Parts pour parent isolé
        System.out.println( "Parent isolé : " + this.parentIsole);
        if (this.parentIsole) {
            if ( this.nombreEnfants > 0 ){
                nombrePartsFoyerFiscal = nombrePartsFoyerFiscal + 0.5;
            }
        }

        //Parts pour veuf avec enfant
        if ( situationFamiliale == SituationFamiliale.VEUF && this.nombreEnfants > 0 ) {
            nombrePartsFoyerFiscal = nombrePartsFoyerFiscal + 1;
        }

        //Parts pour enfant(s) handicapé(s)
        nombrePartsFoyerFiscal = nombrePartsFoyerFiscal + this.nombreEnfantsHandicapes * 0.5;

        System.out.println( "Nombre de parts : " + nombrePartsFoyerFiscal);
    }

    public void calculContributionsExceptionnellesHautsRevenus(){
        contributionExceptionnelleHautsRevenus = 0;
        int i = 0;
        do {
            if ( revenuFiscalReference >= limitesTranchesCEHR[i] && revenuFiscalReference < limitesTranchesCEHR[i+1] ) {
                if ( nombrePartsDeclarant == 1 ) {
                    contributionExceptionnelleHautsRevenus += ( revenuFiscalReference - limitesTranchesCEHR[i] ) * tauxCEHRCelibataire[i];
                } else {
                    contributionExceptionnelleHautsRevenus += ( revenuFiscalReference - limitesTranchesCEHR[i] ) * tauxCEHRCouple[i];
                }
                break;
            } else {
                if ( nombrePartsDeclarant == 1 ) {
                    contributionExceptionnelleHautsRevenus += ( limitesTranchesCEHR[i+1] - limitesTranchesCEHR[i] ) * tauxCEHRCelibataire[i];
                } else {
                    contributionExceptionnelleHautsRevenus += ( limitesTranchesCEHR[i+1] - limitesTranchesCEHR[i] ) * tauxCEHRCouple[i];
                }
            }
            i++;
        } while( i < 5); //Parcours de toute les tranches

        contributionExceptionnelleHautsRevenus = Math.round(contributionExceptionnelleHautsRevenus);
        System.out.println( "Contribution exceptionnelle sur les hauts revenus : " + contributionExceptionnelleHautsRevenus);
    }

    public void calCulImpotDeclarant(){
        revenuImposable = revenuFiscalReference / nombrePartsDeclarant;
        impotDeclarant = 0;
        int i = 0;
        do {
            if ( revenuImposable >= limitesTranchesRevenusImposables[i] && revenuImposable < limitesTranchesRevenusImposables[i+1] ) {
                impotDeclarant += ( revenuImposable - limitesTranchesRevenusImposables[i] ) * tauxImpositionParTranche[i];
                break;
            } else {
                impotDeclarant += ( limitesTranchesRevenusImposables[i+1] - limitesTranchesRevenusImposables[i] ) * tauxImpositionParTranche[i];
            }
            i++;
        } while( i < 5);

        impotDeclarant = impotDeclarant * nombrePartsDeclarant;
        impotDeclarant = Math.round(impotDeclarant);

        System.out.println( "Impôt brut des déclarants : " + impotDeclarant);
    }

    public void calculImpotFoyerFiscalComplet(){
        revenuImposable =  revenuFiscalReference / nombrePartsFoyerFiscal;
        impotFoyerFiscal = 0;
        int i = 0;
        do {
            if ( revenuImposable >= limitesTranchesRevenusImposables[i] && revenuImposable < limitesTranchesRevenusImposables[i+1] ) {
                impotFoyerFiscal += ( revenuImposable - limitesTranchesRevenusImposables[i] ) * tauxImpositionParTranche[i];
                break;
            } else {
                impotFoyerFiscal += ( limitesTranchesRevenusImposables[i+1] - limitesTranchesRevenusImposables[i] ) * tauxImpositionParTranche[i];
            }
            i++;
        } while( i < 5);

        impotFoyerFiscal = impotFoyerFiscal * nombrePartsFoyerFiscal;
        impotFoyerFiscal = Math.round(impotFoyerFiscal);

        System.out.println( "Impôt brut du foyer fiscal complet : " + impotFoyerFiscal);
    }

    public void verificationBaisseImpot(){
        //Plafond de baisse maximal par demi part
        double plafondBaisseMaxDemiPart = 1759;
        double baisseImpot = impotDeclarant - impotFoyerFiscal;
        System.out.println( "Baisse d'impôt : " + baisseImpot );

        //Dépassement plafond
        double ecartPts = nombrePartsFoyerFiscal - nombrePartsDeclarant;
        double plafond = (ecartPts / 0.5) * plafondBaisseMaxDemiPart;
        System.out.println( "Plafond de baisse autorisée " + plafond );

        if ( baisseImpot >= plafond ) {
            impotFoyerFiscal = impotDeclarant - plafond;
        }

        System.out.println( "Impôt brut après plafonnement avant decote : " + impotFoyerFiscal);
        impotAvantDecote = impotFoyerFiscal;
    }

    public void calculDecote(){
        decote = 0;
        double seuilDecoteDeclarantSeul = 1929;
        double seuilDecoteDeclarantCouple = 3191;
        double decoteMaxDeclarantSeul = 873;
        double decoteMaxDeclarantCouple = 1444;
        double tauxDecote = 0.4525;

        // decote
        if ( nombrePartsDeclarant == 1 ) {
            if ( impotFoyerFiscal < seuilDecoteDeclarantSeul) {
                decote = decoteMaxDeclarantSeul - ( impotFoyerFiscal * tauxDecote);
            }
        }
        if (  nombrePartsDeclarant == 2 ) {
            if ( impotFoyerFiscal < seuilDecoteDeclarantCouple) {
                decote =  decoteMaxDeclarantCouple - ( impotFoyerFiscal * tauxDecote);
            }
        }
        //Arrondissement de la decote
        decote = Math.round( decote );

        if ( impotFoyerFiscal <= decote ) {
            decote = impotFoyerFiscal;
        }

        System.out.println( "Decote : " + decote );
        impotFoyerFiscal = impotFoyerFiscal - decote;
        impotFoyerFiscal += contributionExceptionnelleHautsRevenus;
        impotFoyerFiscal = Math.round(impotFoyerFiscal);
        System.out.println( "Impôt sur le revenu net final : " + impotFoyerFiscal);
    }
}