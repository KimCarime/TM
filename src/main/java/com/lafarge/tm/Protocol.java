package com.lafarge.tm;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by klefevre on 06/05/15.
 */
public class Protocol {
    public static final String TAG = "KerlinkProtocol";

    public static final int HEADER = 0xC0;
    public static final int VERSION = 0x01;

    /**
     *  Definition of messages send to the box
     */
    public static final int TRAME_SLUMP_CIBLE = 0x8001;
    public static final int TRAME_VOLUME_EAU_MAXIMUM = 0x8002;
    public static final int TRAME_AUTORISATION_REFUS_AJOUT_EAU = 0x8003;
    public static final int TRAME_ACTIVATION_INHIBITION_AFFICHEUR = 0x8004;
    public static final int TRAME_NOTIFICATION_FIN_DECHARGEMENT = 0x8005;
    public static final int TRAME_NOTIFICATION_ACCEPTATION_COMMANDE = 0x8006;
    public static final int TRAME_VOLUME_CHARGE = 0x8007;

    public static final int TRAME_PARAMETRE_T1 = 0xA001;
    public static final int TRAME_PARAMETRE_A11 = 0xA002;
    public static final int TRAME_PARAMETRE_A12 = 0xA003;
    public static final int TRAME_PARAMETRE_A13 = 0xA004;
    public static final int TRAME_PARAMETRE_NOMBRE_D_AIMANTS = 0xA005;
    public static final int TRAME_PARAMETRE_TEMPS_AVANT_COULANT = 0xA006;
    public static final int TRAME_PARAMETRE_TEMPO_ATTENTE_REPONSE_CONDUCTEUR = 0xA007;
    public static final int TRAME_NOMBRE_D_IMPULSIONS_PAR_LITRE = 0xA008;
    public static final int TRAME_FREQUENCE_DEBITMETRE = 0xA009;
    public static final int TRAME_MODE_DE_COMMANDE_POMPE = 0xA00A;
    public static final int TRAME_FACTEUR_A_CAPTEUR_PRESSION_ENTREE = 0xA00B;
    public static final int TRAME_FACTEUR_B_CAPTEUR_PRESSION_ENTREE = 0xA00C;
    public static final int TRAME_FACTEUR_A_CAPTEUR_PRESSION_SORTIE = 0xA00D;
    public static final int TRAME_FACTEUR_B_CAPTEUR_PRESSION_SORTIE = 0xA00E;
    public static final int TRAME_DUREE_ATTENTE_OUVERTURE_EV1_VA1 = 0xA00F;
    public static final int TRAME_DUREE_ATTENTE_FERMETURE_VA1_EV1 = 0xA010;
    public static final int TRAME_TOLERANCE_DE_COMPTAGE = 0xA011;
    public static final int TRAME_DUREE_ATTENTE_APRES_AJOUT_EAU = 0xA012;
    public static final int TRAME_DELAI_MAXIMUM_AVANT_ECOULEMENT = 0xA013;
    public static final int TRAME_NOMBRE_MAX_ERREURS_ECOULEMENT = 0xA014;
    public static final int TRAME_NOMBRE_MAX_ERREURS_COMPTAGE = 0xA015;

    /**
     *  Definition of messages to received from the box
     */
    public static final int TRAME_SLUMP_COURANT = 0x1001;
    public static final int TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE = 0x1002;

    public static final int TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE = 0x3001;
    public static final int TRAME_NOTIFICATION_PASSAGE_EN_VIDANGE = 0x3002;
    public static final int TRAME_NOTIFICATION_DEBUT_AJOUT_EAU = 0x3003;
    public static final int TRAME_NOTIFICATION_FIN_AJOUT_EAU = 0x3004;
    public static final int TRAME_NOTIFICATION_PARAMETRES_STATIQUES_RECUS = 0x3005;
    public static final int TRAME_NOTIFICATION_PARAMETRES_DYNAMIQUES_RECUS = 0x3006;
    public static final int TRAME_NOTIFICATION_ACCEPTATION_LIVRAISON_RECUE = 0x3007;
    public static final int TRAME_NOTIFICATION_FRANCHISSEMENT_TRANSITION = 0x3008;

    public static final int TRAME_DEMANDE_AUTORISATION_AJOUT_EAU = 0x5001;
    public static final int TRAME_DEMANDE_PARAMETRES_STATIQUES = 0x5002;
    public static final int TRAME_DEMANDE_PARAMETRES_DYNAMIQUES = 0x5003;
    public static final int TRAME_DEMANDE_ACCEPTATION_LIVRAISON = 0x5004;

    public static final int TRAME_TRACE_DEBUG = 0xD001;
    public static final int TRAME_TRACE_DONNEES_BRUTE = 0xD002;
    public static final int TRAME_TRACE_DONNEES_DERIVEES = 0xD003;
    public static final int TRAME_TRACE_DONNEES_INTERNES = 0xD004;
    public static final int TRAME_DONNEES_CALIBRATION = 0xD005;

    public static final int TRAME_NOTIFICATION_ERREUR_EAU_MAX = 0xF001;
    public static final int TRAME_NOTIFICATION_ERREUR_ECOULEMENT = 0xF002;
    public static final int TRAME_NOTIFICATION_ERREUR_COMPTAGE = 0xF003;
    public static final int TRAME_NOTIFICATION_AJOUT_EAU_BLOQUE = 0xF004;
    public static final int TRAME_NOTIFICATION_CAPTEUR_PRESSION_ENTREE_DECONNECTE = 0xF005;
    public static final int TRAME_NOTIFICATION_CAPTEUR_PRESSION_SORTIE_DECONNECTE = 0xF006;
    public static final int TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN = 0xF007;
    public static final int TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX = 0xF008;
//    public static final int REFUS_AJOUT_EAU = 0x00;
//    public static final int AUTORISATION_AJOUT_EAU = 0xFF;
//
//    public static final int INHIBITION_AFFICHEUR = 0x00;
//    public static final int ACTIVATION_AFFICHEUR = 0xFF;
//
//    public static final int COMMANDE_POMPE_SEMI_AUTOMATIQUE = 0x00;
//    public static final int COMMANDE_POMPE_AUTOMATIQUE = 0xFF;
//
//    public static final int AJOUT_MANUEL = 0x00;
//    public static final int AJOUT_AUTOMATIQUE = 0xFF;
//
//    public static final int CRC_VIDE = 0x0000;
//
    /**
     * Other
     */
    public static final int TRAME_BIDON = 0xAFFE;


    /**
     * Spec
     */

    public class SpecTrame {
        public int size;
        public Class[] parameters;
    }

    public static class Pair {
        final int size;

        public Pair(int size, Object type) {
            this.size = size;
        }
    }

    public static final HashMap<Integer, Pair> constants = new HashMap<>();
    static
    {
        constants.put(TRAME_SLUMP_CIBLE, new Pair(2, null));
        constants.put(TRAME_VOLUME_EAU_MAXIMUM, new Pair(2, null));
        constants.put(TRAME_AUTORISATION_REFUS_AJOUT_EAU, new Pair(1, null));
        constants.put(TRAME_ACTIVATION_INHIBITION_AFFICHEUR, new Pair(1, null));
        constants.put(TRAME_NOTIFICATION_FIN_DECHARGEMENT, new Pair(0, null));
        constants.put(TRAME_NOTIFICATION_ACCEPTATION_COMMANDE, new Pair(0, null));
        constants.put(TRAME_VOLUME_CHARGE, new Pair(8, null));
        constants.put(TRAME_PARAMETRE_T1, new Pair(8, null));
        constants.put(TRAME_PARAMETRE_A11, new Pair(8, null));
        constants.put(TRAME_PARAMETRE_A12, new Pair(8, null));
        constants.put(TRAME_PARAMETRE_A13, new Pair(8, null));
        constants.put(TRAME_PARAMETRE_NOMBRE_D_AIMANTS, new Pair(1, null));
        constants.put(TRAME_PARAMETRE_TEMPS_AVANT_COULANT, new Pair(2, null));
        constants.put(TRAME_PARAMETRE_TEMPO_ATTENTE_REPONSE_CONDUCTEUR, new Pair(2, null));
        constants.put(TRAME_NOMBRE_D_IMPULSIONS_PAR_LITRE, new Pair(2, null));
        constants.put(TRAME_FREQUENCE_DEBITMETRE, new Pair(2, null));
        constants.put(TRAME_MODE_DE_COMMANDE_POMPE, new Pair(1, null));
        constants.put(TRAME_FACTEUR_A_CAPTEUR_PRESSION_ENTREE, new Pair(8, null));
        constants.put(TRAME_FACTEUR_B_CAPTEUR_PRESSION_ENTREE, new Pair(8, null));
        constants.put(TRAME_FACTEUR_A_CAPTEUR_PRESSION_SORTIE, new Pair(8, null));
        constants.put(TRAME_FACTEUR_B_CAPTEUR_PRESSION_SORTIE, new Pair(8, null));
        constants.put(TRAME_DUREE_ATTENTE_OUVERTURE_EV1_VA1, new Pair(2, null));
        constants.put(TRAME_DUREE_ATTENTE_FERMETURE_VA1_EV1, new Pair(2, null));
        constants.put(TRAME_TOLERANCE_DE_COMPTAGE, new Pair(1, null));
        constants.put(TRAME_DUREE_ATTENTE_APRES_AJOUT_EAU, new Pair(2, null));
        constants.put(TRAME_DELAI_MAXIMUM_AVANT_ECOULEMENT, new Pair(2, null));
        constants.put(TRAME_NOMBRE_MAX_ERREURS_ECOULEMENT, new Pair(1, null));
        constants.put(TRAME_NOMBRE_MAX_ERREURS_COMPTAGE, new Pair(1, null));
        constants.put(TRAME_SLUMP_COURANT, new Pair(2, null));
        constants.put(TRAME_VOLUME_EAU_AJOUTE_PLUS_MODE, new Pair(2, null));
        constants.put(TRAME_NOTIFICATION_PASSAGE_EN_MALAXAGE, new Pair(0, null));
        constants.put(TRAME_NOTIFICATION_PASSAGE_EN_VIDANGE, new Pair(0, null));
        constants.put(TRAME_NOTIFICATION_DEBUT_AJOUT_EAU, new Pair(0, null));
        constants.put(TRAME_NOTIFICATION_FIN_AJOUT_EAU, new Pair(0, null));
        constants.put(TRAME_NOTIFICATION_PARAMETRES_STATIQUES_RECUS, new Pair(0, null));
        constants.put(TRAME_NOTIFICATION_PARAMETRES_DYNAMIQUES_RECUS, new Pair(0, null));
        constants.put(TRAME_NOTIFICATION_ACCEPTATION_LIVRAISON_RECUE, new Pair(0, null));
        constants.put(TRAME_NOTIFICATION_FRANCHISSEMENT_TRANSITION, new Pair(2, null));
        constants.put(TRAME_DEMANDE_AUTORISATION_AJOUT_EAU, new Pair(1, null));
        constants.put(TRAME_DEMANDE_PARAMETRES_STATIQUES, new Pair(0, null));
        constants.put(TRAME_DEMANDE_PARAMETRES_DYNAMIQUES, new Pair(0, null));
        constants.put(TRAME_DEMANDE_ACCEPTATION_LIVRAISON, new Pair(0, null));
        constants.put(TRAME_TRACE_DEBUG, new Pair(0/*?*/, null));
        constants.put(TRAME_TRACE_DONNEES_BRUTE, new Pair(13, null));
        constants.put(TRAME_TRACE_DONNEES_DERIVEES, new Pair(6, null));
        constants.put(TRAME_TRACE_DONNEES_INTERNES, new Pair(6, null));
        constants.put(TRAME_DONNEES_CALIBRATION, new Pair(12, null));
        constants.put(TRAME_NOTIFICATION_ERREUR_EAU_MAX, new Pair(0, null));
        constants.put(TRAME_NOTIFICATION_ERREUR_ECOULEMENT, new Pair(0, null));
        constants.put(TRAME_NOTIFICATION_ERREUR_COMPTAGE, new Pair(0, null));
        constants.put(TRAME_NOTIFICATION_AJOUT_EAU_BLOQUE, new Pair(0, null));
        constants.put(TRAME_NOTIFICATION_CAPTEUR_PRESSION_ENTREE_DECONNECTE, new Pair(1, null));
        constants.put(TRAME_NOTIFICATION_CAPTEUR_PRESSION_SORTIE_DECONNECTE, new Pair(1, null));
        constants.put(TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MIN, new Pair(1, null));
        constants.put(TRAME_NOTIFICATION_CAPTEUR_VITESSE_SEUIL_MAX, new Pair(1, null));
    }

}
