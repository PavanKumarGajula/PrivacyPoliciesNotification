package com.privacypolicies.PrivacyPoliciesNotification.Service;

import com.github.difflib.DiffUtils;
import com.github.difflib.patch.Patch;
import com.privacypolicies.PrivacyPoliciesNotification.Model.PrivacyOfWeb;
import com.privacypolicies.PrivacyPoliciesNotification.Repository.WebScrapingRepo;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

@Service
public class WebScrappingService {

    @Autowired
    private WebScrapingRepo webScrapingRepo;

    private static final String[] PRIVACY_KEYWORDS = {"privacy", "privacy policy"};

    public String fetchPrivacyPolicy(PrivacyOfWeb privacyOfWeb, String baseUrl) {
        String isSaved = "Data not saved";
        try {
            Document homePage = Jsoup.connect(baseUrl).get();
            Element privacyLinkElement = findPrivacyLink(homePage);

            if (privacyLinkElement != null) {
                String privacyUrl = privacyLinkElement.absUrl("href");
                Document privacyPolicyPage = Jsoup.connect(privacyUrl)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.3")
                        .timeout(10 * 1000)
                        .get();
                String policyText = privacyPolicyPage.text();
                int noOfRowsInserted = webScrapingRepo.saveWebPolicy(privacyOfWeb, policyText);

                if (noOfRowsInserted > 0) {
                    isSaved = "This version of policy saved";
                }
            }
        } catch (HttpStatusException e) {
            isSaved = "HTTP error fetching URL. Status=" + e.getStatusCode() + ", URL=" + e.getUrl();
        } catch (IOException e) {
            isSaved = "Error fetching URL: " + e.getMessage();
        }
        return isSaved;
    }


    private Element findPrivacyLink(Document document) {
        Elements links = document.select("a[href]");
        for (Element link : links) {
            for (String keyword : PRIVACY_KEYWORDS) {
                if (link.text().toLowerCase().contains(keyword.toLowerCase())) {
                    return link;
                }
            }
        }
        return null;
    }


    public String anyDiff(){
        List<PrivacyOfWeb> listValues = webScrapingRepo.thePreviousOne();
        StringBuilder differences = new StringBuilder();

        for( PrivacyOfWeb listValue : listValues){
            String prevPolicy = listValue.getPreviousPolicy();
            String updatedPolicy = listValue.getUpdatedPolicy();

            List<String> original = Arrays.asList(prevPolicy.split("/n"));
            List<String> revised = Arrays.asList(updatedPolicy.split("/n"));
            Patch<String> patch = DiffUtils.diff(original, revised);
            if (!patch.getDeltas().isEmpty()) {
                differences.append("Differences found:\n");
                patch.getDeltas().forEach(delta -> differences.append(delta.toString()).append("\n"));
            }
        }
        if (differences.length() == 0) {
            return "No differences found.";
        } else {
            return differences.toString();
        }
    }

    public String showPolicy() {
        String policy = webScrapingRepo.getPolicy();
        return policy;
    }
}
