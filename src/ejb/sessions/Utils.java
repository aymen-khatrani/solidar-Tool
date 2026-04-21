package ejb.sessions;

import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

public class Utils {
  
  /**
   * retourne le nombre de km entre 2 points
   * @param lat1 latitude du point 1
   * @param lon1 longitude du point 1
   * @param lat2 latitude du point 2
   * @param lon2 longitude du point 2
   * @return nb de kms qui sépare les 2 points.
   */
  public static double calculerDistance(double lat1, double lon1, double lat2, double lon2) {
      final double RAYON_TERRE_KM = 6371; // Rayon moyen de la Terre en kilomètres
      // Convertir les degrés en radians
      double lat1Rad = Math.toRadians(lat1);
      double lon1Rad = Math.toRadians(lon1);
      double lat2Rad = Math.toRadians(lat2);
      double lon2Rad = Math.toRadians(lon2);

      // Différences de latitude et longitude
      double dLat = lat2Rad - lat1Rad;
      double dLon = lon2Rad - lon1Rad;

      // Formule de Haversine
      double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                 Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                 Math.sin(dLon / 2) * Math.sin(dLon / 2);
      double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
      double distance = RAYON_TERRE_KM * c;

      return distance;
  }

  public static final java.sql.Date today = java.sql.Date.valueOf(LocalDate.now());

  /**
	 * retourne le nombre de jours n de différences avec la date du jour
	 * si n < 0 , la date d est antérieure à la date du jour
	 * @param d date à évaluer
	 * @return nombre de jours
	 */
	public static long nbJoursDifferenceAvecDateDuJour(java.sql.Date d) {
		LocalDate dateDuJour = LocalDate.now(); // Date du jour
		LocalDate d_local = d.toLocalDate();
		return - ChronoUnit.DAYS.between(d_local, dateDuJour);
	}

  /**
   * retourne vrai si la date d1 est supérieure à d2
   * @param d1
   * @param d2
   * @return
   */
  public static boolean isAfter(java.sql.Date d1, java.sql.Date d2) {
    LocalDate ld1=d1.toLocalDate();
    LocalDate ld2=d2.toLocalDate();
    return ChronoUnit.DAYS.between(ld1, ld2) < 0;
  }
  /**
   * retourne vrai si la date d1 est inférieure à d2
   * @param d1
   * @param d2
   * @return
   */
  public static boolean isBefore(java.sql.Date d1, java.sql.Date d2) {
    LocalDate ld1=d1.toLocalDate();
    LocalDate ld2=d2.toLocalDate();
    return ChronoUnit.DAYS.between(ld1, ld2) > 0;
  }

  /**
   * retourne vrai si la date d1 est inférieure à d2
   * @param d1
   * @param d2
   * @return
   */
  public static boolean isEqual(java.sql.Date d1, java.sql.Date d2) {
    LocalDate ld1=d1.toLocalDate();
    LocalDate ld2=d2.toLocalDate();
    return ChronoUnit.DAYS.between(ld1, ld2) == 0;
  }

  public static void main(String[] args) {
      // Exemple : Distance entre Paris (48.8566, 2.3522) et Lyon (45.7640, 4.8357)
      double lat1 = 48.8566;
      double lon1 = 2.3522;
      double lat2 = 45.7640;
      double lon2 = 4.8357;

      System.out.println("Distance en km entre Paris et Lyon") ;
      double distance = Utils.calculerDistance(lat1, lon1, lat2, lon2);
      System.out.printf("Distance : %.2f km%n", distance);


      System.out.print("est-ce que la date du jour est après le 19 février 2026 ? :" ) ;
      System.out.println(Utils.isAfter(Utils.today,Date.valueOf("2026-02-19"))) ;
  }
}

