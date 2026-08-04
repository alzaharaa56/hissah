package com.hissah.Utilities;

import org.springframework.stereotype.Component;

import java.time.Year;
import java.util.UUID;

@Component
public class ReferenceNumberGenerator {


    public String generateProjectReference(String sectorCode) {
        String year = String.valueOf(Year.now().getValue());
        String uniquePart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        String sector = (sectorCode != null && !sectorCode.isEmpty()) ? sectorCode.toUpperCase() : "GEN";
        return String.format("PRJ-%s-%s-%s", sector, year, uniquePart);
    }


    public String generatePackageReference() {
        String year = String.valueOf(Year.now().getValue());
        String uniquePart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("PKG-%s-%s", year, uniquePart);
    }


    public String generateBidReference() {
        String year = String.valueOf(Year.now().getValue());
        String uniquePart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("BID-%s-%s", year, uniquePart);
    }


    public String generateAwardReference() {
        String year = String.valueOf(Year.now().getValue());
        String uniquePart = UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("AWD-%s-%s", year, uniquePart);
    }
}
