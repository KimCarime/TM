package com.lafarge.truckmix;

import java.util.HashMap;

public class Protocol {
    public static final String TAG = "KerlinkProtocol";

    public static final int HEADER = 0xC0;
    public static final int VERSION = 0x01;

    /**
     *  Definition of messages send to the box
     */
    public static final String TRAME_SLUMP_CIBLE = "TRAME_SLUMP_CIBLE";
    public static final String TRAME_VOLUME_EAU_MAXIMUM = "TRAME_VOLUME_EAU_MAXIMUM";
    public static final String TRAME_AUTORISATION_REFUS_AJOUT_EAU = "TRAME_AUTORISATION_REFUS_AJOUT_EAU";
    public static final String TRAME_ACTIVATION_INHIBITION_AFFICHEUR = "TRAME_ACTIVATION_INHIBITION_AFFICHEUR";
    public static final String TRAME_NOTIFICATION_FIN_DECHARGEMENT = "TRAME_NOTIFICATION_FIN_DECHARGEMENT";
    public static final String TRAME_NOTIFICATION_ACCEPTATION_COMMANDE = "TRAME_NOTIFICATION_ACCEPTATION_COMMANDE";
    public static final String TRAME_VOLUME_CHARGE = "TRAME_VOLUME_CHARGE";
    public static final String TRAME_PARAMETRE_T1 = "TRAME_PARAMETRE_T1";
    public static final String TRAME_PARAMETRE_A11 = "TRAME_PARAMETRE_A11";
    public static final String TRAME_PARAMETRE_A12 = "TRAME_PARAMETRE_A12";
    public static final String TRAME_PARAMETRE_A13 = "TRAME_PARAMETRE_A13";
    public static final String TRAME_PARAMETRE_NOMBRE_D_AIMANTS = "TRAME_PARAMETRE_NOMBRE_D_AIMANTS";
    public static final String TRAME_PARAMETRE_TEMPS_AVANT_COULANT = "TRAME_PARAMETRE_TEMPS_AVANT_COULANT";
    public static final String TRAME_PARAMETRE_TEMPO_ATTENTE_REPONSE_CONDUCTEUR = "TRAME_PARAMETRE_TEMPO_ATTENTE_REPONSE_CONDUCTEUR";
    public static final String TRAME_NOMBRE_D_IMPULSIONS_PAR_LITRE = "TRAME_NOMBRE_D_IMPULSIONS_PAR_LITRE";
    public static final String TRAME_FREQUENCE_DEBITMETRE = "TRAME_FREQUENCE_DEBITMETRE";
    public static final String TRAME_MODE_DE_COMMANDE_POMPE = "TRAME_MODE_DE_COMMANDE_POMPE";
    public static final String TRAME_FACTEUR_A_CAPTEUR_PRESSION_ENTREE = "TRAME_FACTEUR_A_CAPTEUR_PRESSION_ENTREE";
    public static final String TRAME_FACTEUR_B_CAPTEUR_PRESSION_ENTREE = "TRAME_FACTEUR_B_CAPTEUR_PRESSION_ENTREE";
    public static final String TRAME_FACTEUR_A_CAPTEUR_PRESSION_SORTIE = "TRAME_FACTEUR_A_CAPTEUR_PRESSION_SORTIE";
    public static final String TRAME_FACTEUR_B_CAPTEUR_PRESSION_SORTIE = "TRAME_FACTEUR_B_CAPTEUR_PRESSION_SORTIE";
    public static final String TRAME_DUREE_ATTENTE_OUVERTURE_EV1_VA1 = "TRAME_DUREE_ATTENTE_OUVERTURE_EV1_VA1";
    public static final String TRAME_DUREE_ATTENTE_FERMETURE_VA1_EV1 = "TRAME_DUREE_ATTENTE_FERMETURE_VA1_EV1";
    public static final String TRAME_TOLERANCE_DE_COMPTAGE = "TRAME_TOLERANCE_DE_COMPTAGE";
    public static final String TRAME_DUREE_ATTENTE_APRES_AJOUT_EAU = "TRAME_DUREE_ATTENTE_APRES_AJOUT_EAU";
    public static final String TRAME_DELAI_MAXIMUM_AVANT_ECOULEMENT = "TRAME_DELAI_MAXIMUM_AVANT_ECOULEMENT";
    public static final String TRAME_NOMBRE_MAX_ERREURS_ECOULEMENT = "TRAME_NOMBRE_MAX_ERREURS_ECOULEMENT";
    public static final String TRAME_NOMBRE_MAX_ERREURS_COMPTAGE = "TRAME_NOMBRE_MAX_ERREURS_COMPTAGE";

    /**
     *  Definition of messages to received from the box
     */
    public static final String TRAME_SLUMP_COURANT = "TRAME_SLUMP_COURANT";
    public static final String TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE = "TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE";
    public static final String TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE = "TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE";
    public static final String TRAME_NOTIFICATION_PASSAGE_EN_VIDANGE = "TRAME_NOTIFICATION_PASSAGE_EN_VIDANGE";
    public static final String TRAME_NOTIFICATION_DEBUT_AJOUT_EAU = "TRAME_NOTIFICATION_DEBUT_AJOUT_EAU";
    public static final String TRAME_NOTIFICATION_FIN_AJOUT_EAU = "TRAME_NOTIFICATION_FIN_AJOUT_EAU";
    public static final String TRAME_NOTIFICATION_PARAMETRES_STATIQUES_RECUS = "TRAME_NOTIFICATION_PARAMETRES_STATIQUES_RECUS";
    public static final String TRAME_NOTIFICATION_PARAMETRES_DYNAMIQUES_RECUS = "TRAME_NOTIFICATION_PARAMETRES_DYNAMIQUES_RECUS";
    public static final String TRAME_NOTIFICATION_ACCEPTATION_LIVRAISON_RECUE = "TRAME_NOTIFICATION_ACCEPTATION_LIVRAISON_RECUE";
    public static final String TRAME_NOTIFICATION_FRANCHISSEMENT_TRANSITION = "TRAME_NOTIFICATION_FRANCHISSEMENT_TRANSITION";
    public static final String TRAME_DEMANDE_AUTORISATION_AJOUT_EAU = "TRAME_DEMANDE_AUTORISATION_AJOUT_EAU";
    public static final String TRAME_DEMANDE_PARAMETRES_STATIQUES = "TRAME_DEMANDE_PARAMETRES_STATIQUES";
    public static final String TRAME_DEMANDE_PARAMETRES_DYNAMIQUES = "TRAME_DEMANDE_PARAMETRES_DYNAMIQUES";
    public static final String TRAME_DEMANDE_ACCEPTATION_LIVRAISON = "TRAME_DEMANDE_ACCEPTATION_LIVRAISON";
    public static final String TRAME_TRACE_DEBUG = "TRAME_TRACE_DEBUG";
    public static final String TRAME_DONNEES_BRUTES = "TRAME_DONNEES_BRUTES";
    public static final String TRAME_DONNEES_DERIVEES = "TRAME_DONNEES_DERIVEES";
    public static final String TRAME_DONNEES_INTERNES = "TRAME_DONNEES_INTERNES";
    public static final String TRAME_DONNEES_CALIBRATION = "TRAME_DONNEES_CALIBRATION";
    public static final String TRAME_NOTIFICATION_ERREUR_EAU_MAX = "TRAME_NOTIFICATION_ERREUR_EAU_MAX";
    public static final String TRAME_NOTIFICATION_ERREUR_ECOULEMENT = "TRAME_NOTIFICATION_ERREUR_ECOULEMENT";
    public static final String TRAME_NOTIFICATION_ERREUR_COMPTAGE = "TRAME_NOTIFICATION_ERREUR_COMPTAGE";
    public static final String TRAME_NOTIFICATION_AJOUT_EAU_BLOQUE = "TRAME_NOTIFICATION_AJOUT_EAU_BLOQUE";
    public static final String TRAME_NOTIFICATION_CAPTEUR_PRESSION_ENTREE_DECONNECTE = "TRAME_NOTIFICATION_CAPTEUR_PRESSION_ENTREE_DECONNECTE";
    public static final String TRAME_NOTIFICATION_CAPTEUR_PRESSION_SORTIE_DECONNECTE = "TRAME_NOTIFICATION_CAPTEUR_PRESSION_SORTIE_DECONNECTE";
    public static final String TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN = "TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN";
    public static final String TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX = "TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX";

    /**
     * Other
     */
    public static final int TRAME_BIDON = 0xAFFE;

    /**
     * Spec
     */
    public static class Spec {
        public static final int SIZE_UNDEFINED = -1;

        public final int address;
        public final int size;
        public final int[] booleansToCheck;

        public Spec(int address, int size) {
            this(address, size, null);
        }
        public Spec(int address, int size, int[] booleansToCheck) {
            this.address = address;
            this.size = size;
            this.booleansToCheck = booleansToCheck;
        }
    }

    public static final HashMap<String, Spec> constants = new HashMap<String, Spec>();
    static
    {
        constants.put(TRAME_SLUMP_CIBLE, new Spec(0x8001, 2));
        constants.put(TRAME_VOLUME_EAU_MAXIMUM, new Spec(0x8002, 2));
        constants.put(TRAME_AUTORISATION_REFUS_AJOUT_EAU, new Spec(0x8003, 1));
        constants.put(TRAME_ACTIVATION_INHIBITION_AFFICHEUR, new Spec(0x8004, 1));
        constants.put(TRAME_NOTIFICATION_FIN_DECHARGEMENT, new Spec(0x8005, 0));
        constants.put(TRAME_NOTIFICATION_ACCEPTATION_COMMANDE, new Spec(0x8006, 0));
        constants.put(TRAME_VOLUME_CHARGE, new Spec(0x8007, 8));
        constants.put(TRAME_PARAMETRE_T1, new Spec(0xA001, 8));
        constants.put(TRAME_PARAMETRE_A11, new Spec(0xA002, 8));
        constants.put(TRAME_PARAMETRE_A12, new Spec(0xA003, 8));
        constants.put(TRAME_PARAMETRE_A13, new Spec(0xA004, 8));
        constants.put(TRAME_PARAMETRE_NOMBRE_D_AIMANTS, new Spec(0xA005, 1));
        constants.put(TRAME_PARAMETRE_TEMPS_AVANT_COULANT, new Spec(0xA006, 2));
        constants.put(TRAME_PARAMETRE_TEMPO_ATTENTE_REPONSE_CONDUCTEUR, new Spec(0xA007, 2));
        constants.put(TRAME_NOMBRE_D_IMPULSIONS_PAR_LITRE, new Spec(0xA008, 2));
        constants.put(TRAME_FREQUENCE_DEBITMETRE, new Spec(0xA009, 1));
        constants.put(TRAME_MODE_DE_COMMANDE_POMPE, new Spec(0xA00A, 1));
        constants.put(TRAME_FACTEUR_A_CAPTEUR_PRESSION_ENTREE, new Spec(0xA00B, 8));
        constants.put(TRAME_FACTEUR_B_CAPTEUR_PRESSION_ENTREE, new Spec(0xA00C, 8));
        constants.put(TRAME_FACTEUR_A_CAPTEUR_PRESSION_SORTIE, new Spec(0xA00D, 8));
        constants.put(TRAME_FACTEUR_B_CAPTEUR_PRESSION_SORTIE, new Spec(0xA00E, 8));
        constants.put(TRAME_DUREE_ATTENTE_OUVERTURE_EV1_VA1, new Spec(0xA00F, 2));
        constants.put(TRAME_DUREE_ATTENTE_FERMETURE_VA1_EV1, new Spec(0xA010, 2));
        constants.put(TRAME_TOLERANCE_DE_COMPTAGE, new Spec(0xA011, 1));
        constants.put(TRAME_DUREE_ATTENTE_APRES_AJOUT_EAU, new Spec(0xA012, 2));
        constants.put(TRAME_DELAI_MAXIMUM_AVANT_ECOULEMENT, new Spec(0xA013, 2));
        constants.put(TRAME_NOMBRE_MAX_ERREURS_ECOULEMENT, new Spec(0xA014, 1));
        constants.put(TRAME_NOMBRE_MAX_ERREURS_COMPTAGE, new Spec(0xA015, 1));

        constants.put(TRAME_SLUMP_COURANT, new Spec(0x1001, 2));
        constants.put(TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE, new Spec(0x1002, 2, new int[]{1}));
        constants.put(TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE, new Spec(0x3001, 0));
        constants.put(TRAME_NOTIFICATION_PASSAGE_EN_VIDANGE, new Spec(0x3002, 0));
        constants.put(TRAME_NOTIFICATION_DEBUT_AJOUT_EAU, new Spec(0x3003, 0));
        constants.put(TRAME_NOTIFICATION_FIN_AJOUT_EAU, new Spec(0x3004, 0));
        constants.put(TRAME_NOTIFICATION_PARAMETRES_STATIQUES_RECUS, new Spec(0x3005, 0));
        constants.put(TRAME_NOTIFICATION_PARAMETRES_DYNAMIQUES_RECUS, new Spec(0x3006, 0));
        constants.put(TRAME_NOTIFICATION_ACCEPTATION_LIVRAISON_RECUE, new Spec(0x3007, 0));
        constants.put(TRAME_NOTIFICATION_FRANCHISSEMENT_TRANSITION, new Spec(0x3008, 2));
        constants.put(TRAME_DEMANDE_AUTORISATION_AJOUT_EAU, new Spec(0x5001, 1));
        constants.put(TRAME_DEMANDE_PARAMETRES_STATIQUES, new Spec(0x5002, 0));
        constants.put(TRAME_DEMANDE_PARAMETRES_DYNAMIQUES, new Spec(0x5003, 0));
        constants.put(TRAME_DEMANDE_ACCEPTATION_LIVRAISON, new Spec(0x5004, 0));
        constants.put(TRAME_TRACE_DEBUG, new Spec(0xD001, Spec.SIZE_UNDEFINED));
        constants.put(TRAME_DONNEES_BRUTES, new Spec(0xD002, 13, new int[]{12}));
        constants.put(TRAME_DONNEES_DERIVEES, new Spec(0xD003, 6, new int[]{0, 1}));
        constants.put(TRAME_DONNEES_INTERNES, new Spec(0xD004, 6, new int[]{0, 1, 2, 3, 4, 5}));
        constants.put(TRAME_DONNEES_CALIBRATION, new Spec(0xD005, 12));
        constants.put(TRAME_NOTIFICATION_ERREUR_EAU_MAX, new Spec(0xF001, 0));
        constants.put(TRAME_NOTIFICATION_ERREUR_ECOULEMENT, new Spec(0xF002, 0));
        constants.put(TRAME_NOTIFICATION_ERREUR_COMPTAGE, new Spec(0xF003, 0));
        constants.put(TRAME_NOTIFICATION_AJOUT_EAU_BLOQUE, new Spec(0xF004, 0));
        constants.put(TRAME_NOTIFICATION_CAPTEUR_PRESSION_ENTREE_DECONNECTE, new Spec(0xF005, 1, new int[]{0}));
        constants.put(TRAME_NOTIFICATION_CAPTEUR_PRESSION_SORTIE_DECONNECTE, new Spec(0xF006, 1, new int[]{0}));
        constants.put(TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN, new Spec(0xF007, 1, new int[]{0}));
        constants.put(TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX, new Spec(0xF008, 1, new int[]{0}));
    }
}
