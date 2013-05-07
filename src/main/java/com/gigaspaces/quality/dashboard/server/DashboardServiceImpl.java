package com.gigaspaces.quality.dashboard.server;

import com.gigaspaces.quality.dashboard.client.DashboardService;
import com.gigaspaces.quality.dashboard.server.utils.Utils;
import com.gigaspaces.quality.dashboard.shared.CompoundSuiteHistoryResult;
import com.gigaspaces.quality.dashboard.shared.SuiteHistory;
import com.gigaspaces.quality.dashboard.shared.SuiteResult;
import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import java.util.*;


/**
 * The server side implementation of the RPC service.
 */
@SuppressWarnings("serial")
public class DashboardServiceImpl extends RemoteServiceServlet implements DashboardService {

    private EntityManagerFactory entityManagerFactory;
    private static Properties props = new Properties();

    static {
        props = Utils.loadPropertiesFromClasspath("com/gigaspaces/quality/dashboard/server/versions.properties");
    }

    public DashboardServiceImpl() {
        ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("com/gigaspaces/quality/dashboard/config.xml");
        entityManagerFactory = ctx.getBean("entityManagerFactory", EntityManagerFactory.class);
    }

    @Override
    public Map<String, CompoundSuiteHistoryResult> submitSuiteResultQuery() {
        EntityManager entityManager = null;
        try{
            entityManager = entityManagerFactory.createEntityManager();

            List<String> xapVersions = getXAPVersions(entityManager);
            Map<String, List<SuiteResult>> results = new HashMap<String, List<SuiteResult>>();

            String sqlFromQueryPart = "select * from ( select * from SgtestResult order by timestamp desc ) as t group by buildVersion, suiteName, jvmType";

            List<SuiteResult> suiteResults = new ArrayList<SuiteResult>(
                    entityManager.createNativeQuery(sqlFromQueryPart, SuiteResult.class).getResultList());

            List<SuiteResult> tmpResult = null;
            for(String xapVersion : xapVersions){
                String cloudifyVersion = props.getProperty("version" + xapVersion);
                tmpResult = new ArrayList<SuiteResult>();
                for(SuiteResult suiteResult : suiteResults){
                    if(suiteResult.getCompoundKey().getBuildVersion().equals(xapVersion) || suiteResult.getCompoundKey().getBuildVersion().equals(cloudifyVersion)){
                        tmpResult.add(suiteResult);
                    }
                }
                results.put(xapVersion, tmpResult);
            }

            Map<String, CompoundSuiteHistoryResult> compoundSuiteHistoryResults = new HashMap<String, CompoundSuiteHistoryResult>();

            Set<String> keysMap = results.keySet();
            for(String xapVersion : keysMap){
                CompoundSuiteHistoryResult suiteHistoryResult = new CompoundSuiteHistoryResult();
                suiteHistoryResult.setResults(results.get(xapVersion));
                Map<String, List<SuiteHistory>> suitePassedTests = new HashMap<String, List<SuiteHistory>>();
                for(SuiteResult result : suiteHistoryResult.getResults()){
                    List<SuiteHistory> passedTestsHistory = getSuiteHistory(entityManager, result);
                    suitePassedTests.put(result.getCompoundKey().getSuiteName(), passedTestsHistory);
                }
                suiteHistoryResult.setSuiteHistory(suitePassedTests);
                compoundSuiteHistoryResults.put(xapVersion, suiteHistoryResult);
            }
            sortAccordingFailedTests(compoundSuiteHistoryResults);
            return compoundSuiteHistoryResults;
        }finally{
            if(entityManager != null)
                entityManager.close();
        }

    }

    private List<SuiteHistory> getSuiteHistory(EntityManager entityManager, SuiteResult result) {
        List<SuiteHistory> passedTestsHistoryResults = entityManager.createQuery("select new com.gigaspaces.quality.dashboard.shared.SuiteHistory( " +
                "t1.compoundKey.buildNumber, t1.compoundKey.buildVersion," +
                " t1.compoundKey.milestone, t1.passedTests, t1.totalTestsRun, t1.skippedTests, t1.suspectedTests, t1.timestamp, t1.type) from " +
                "SgtestResult as t1 where t1.compoundKey.buildVersion = '" + result.getCompoundKey().getBuildVersion() + "'"
                + " and t1.compoundKey.suiteName = '" + result.getCompoundKey().getSuiteName() + "'" + " order by t1.timestamp desc ", SuiteHistory.class)
                .setMaxResults(10).getResultList();

        return new ArrayList<SuiteHistory>(passedTestsHistoryResults);
    }

    private List<String> getXAPVersions(EntityManager entityManager) {
        List<String> allVersions = entityManager.createQuery("select distinct t1.compoundKey.buildVersion from SgtestResult as t1", String.class).getResultList();
        List<String> xapVersions = new ArrayList<String>();
        for(String version : allVersions){
            String[] tokens = version.split("\\.");
            if(Integer.valueOf(tokens[0]) > 8){
                xapVersions.add(version);
            }
        }
        return xapVersions;
    }

    private void sortAccordingFailedTests(Map<String, CompoundSuiteHistoryResult> results){
        for(CompoundSuiteHistoryResult  compoundSuiteHistoryResult : results.values()){
            Collections.sort(compoundSuiteHistoryResult.getResults(), new Comparator<SuiteResult>(){
                public int compare(SuiteResult s1, SuiteResult s2) {
                    double diff = (s1.getPassedTests() / (double)( s1.getTotalTestsRun() - s1.getSuspectedTests())) -
                            (s2.getPassedTests() / (double) (s2.getTotalTestsRun() - s2.getSuspectedTests()));
                    return diff > 0 ? 1 : diff < 0 ? -1 : 0;
                }
            });
        }
    }
}
