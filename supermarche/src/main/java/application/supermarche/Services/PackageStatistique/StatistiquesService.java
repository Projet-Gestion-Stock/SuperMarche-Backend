package application.supermarche.Services.PackageStatistique;

import java.util.Map;

public interface StatistiquesService {
    Map<String, Object> statistiquesVentes();
    Map<String, Object> produitsPlusVendus(int limit);
}
