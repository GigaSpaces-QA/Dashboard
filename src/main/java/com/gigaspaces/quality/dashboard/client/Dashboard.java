package com.gigaspaces.quality.dashboard.client;


import com.extjs.gxt.ui.client.event.BaseEvent;
import com.extjs.gxt.ui.client.event.Events;
import com.extjs.gxt.ui.client.event.Listener;
import com.extjs.gxt.ui.client.util.Margins;
import com.extjs.gxt.ui.client.widget.ContentPanel;
import com.extjs.gxt.ui.client.widget.Dialog;
import com.extjs.gxt.ui.client.widget.LayoutContainer;
import com.extjs.gxt.ui.client.widget.layout.BoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayout;
import com.extjs.gxt.ui.client.widget.layout.HBoxLayoutData;
import com.extjs.gxt.ui.client.widget.layout.RowLayout;
import com.gigaspaces.quality.dashboard.client.icons.IconsRepository;
import com.gigaspaces.quality.dashboard.shared.CompoundSuiteHistoryResult;
import com.gigaspaces.quality.dashboard.shared.SuiteHistory;
import com.gigaspaces.quality.dashboard.shared.SuiteResult;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import org.highchartsgwt.client.Chart;
import org.highchartsgwt.client.formatters.DoubleValueFormatter;
import org.highchartsgwt.client.formatters.GraphSeriesPointFormatter;
import org.highchartsgwt.client.options.ChartOptions;
import org.highchartsgwt.client.options.SeriesOptions;
import org.highchartsgwt.client.utils.SeriesPoint;
import org.highchartsgwt.client.utils.SeriesType;

import java.util.*;


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Dashboard implements EntryPoint {
    public static final String SUITE_REPORT_LINK = "suite report link";
    public static final String TGRID = "tgrid";
    public static final String NIGHTLY_REGRESSION = "nightly regression";
    public static final String CPP_REGRESSION = "cpp regression";


    //private List<VerticalPanel> verticalPanels = new ArrayList<VerticalPanel>();
    private DashboardServiceAsync dashboardServiceAsyncService = GWT.create( DashboardService.class );
    private int selectedItem = 0;
    private Chart chart;
    private AbsolutePanel main = new AbsolutePanel();
    private DateTimeFormat dateFormatter = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss.S");
    private DateTimeFormat dateFormatterNoSec = DateTimeFormat.getFormat("dd/MM/yyyy HH:mm");
    private NumberFormat percentage = NumberFormat.getFormat("#.#%");

    private class ProductVerticalPanel extends VerticalPanel{

        private int curComponentsCountPerLastRow;
        private HorizontalPanel curRowPanel;

        public ProductVerticalPanel(){

        }

        public void createAndAddRow(){
            curRowPanel = new HorizontalPanel();
            add( curRowPanel );
        }

        public void addToRow( Widget widget ){
           curRowPanel.add( widget );
        }

        public void incrementComponentsCount(){
            curComponentsCountPerLastRow++;
        }

        public int getComponentsCount(){
            return curComponentsCountPerLastRow;
        }

        public void resetComponentsCount(){
               curComponentsCountPerLastRow=0;
        }
    }

    public void onModuleLoad() {
        RootPanel.getBodyElement().getStyle().setBackgroundColor("black");
        final TabPanel mainPanel = new TabPanel();
        mainPanel.setSize( "100%", "100%");
        mainPanel.getElement().getStyle().setMarginLeft(5, Unit.PX);
        mainPanel.getElement().getStyle().setMarginTop(5, Unit.PX);
        mainPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                selectedItem = event.getSelectedItem();

            }
        });
        main.add(mainPanel);
        RootPanel.get("resultGrid").add(main);

        refresh(mainPanel);

        Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
            @Override
            public boolean execute() {
                return refresh(mainPanel);
            }
        }, 5 * 60 * 1000);


    }

    private void clear(final TabPanel mainPanel) {
        mainPanel.clear();
    }

    private boolean refresh(final TabPanel mainPanel) {
        dashboardServiceAsyncService.submitSuiteResultQuery(new AsyncCallback<Map<String, CompoundSuiteHistoryResult>>() {
            @Override
            public void onSuccess(Map<String, CompoundSuiteHistoryResult> compoundSuiteHistoryResults) {
                clear(mainPanel);
                Set<String> keysMap = compoundSuiteHistoryResults.keySet();
                ArrayList<String> versions = new ArrayList<String>(keysMap);
                Collections.sort(versions);
                Collections.reverse(versions);

                for(String xapVersion : versions){
                HorizontalPanel tabPanel = new HorizontalPanel();
                    ProductVerticalPanel xapPanel = new ProductVerticalPanel();
                    ProductVerticalPanel cloudifyPanel = new ProductVerticalPanel();

                    tabPanel.add(xapPanel);
                    tabPanel.add(cloudifyPanel);
                    String cloudifyVersion = "";

                    for(SuiteResult result : compoundSuiteHistoryResults.get(xapVersion).getResults()){
                        List<SuiteHistory> history = compoundSuiteHistoryResults.get(xapVersion).getSuiteHistory().get(result.getCompoundKey().getSuiteName());
                        String suiteType = result.getType();
                        try{
                        if(suiteType.equalsIgnoreCase("tgrid") || suiteType.equalsIgnoreCase("iTests-XAP")){
                            addSuiteResult(xapPanel, result, history);
                            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&& XAP" + result.getCompoundKey().getSuiteName());
                        }else{
                            addSuiteResult(cloudifyPanel, result, history);
                            System.out.println("&&&&&&&&&&&&&&&&&&&&&&&&&&&&& CLOUDIFY" + result.getCompoundKey().getSuiteName());
                        }
                        }catch (Throwable t){
                            t.printStackTrace();
                        }
                        if(cloudifyVersion.equals("") && history.size() != 0 && !xapVersion.equals(history.get(0).getBuildVersion()))
                            cloudifyVersion = "/" + history.get(0).getBuildVersion();
                    }

                    mainPanel.add(tabPanel, xapVersion + cloudifyVersion);
                }
                mainPanel.selectTab(selectedItem);
            }
            @Override
            public void onFailure(Throwable caught) {
                caught.printStackTrace();

            }
        });
        return true;
    }

    public void addSuiteResult(ProductVerticalPanel panel, SuiteResult suiteResult, List<SuiteHistory> history){
        int suiteResultsCellPerRow = 3;

        if( panel.getComponentsCount() == 0 || panel.getComponentsCount() == suiteResultsCellPerRow ){
            panel.createAndAddRow();
            panel.resetComponentsCount();
        }
        Widget suiteResultCell = createSuiteResultsGridCell(suiteResult, history, suiteResultsCellPerRow);
        panel.incrementComponentsCount();
        panel.addToRow( suiteResultCell );
    }


    private Widget createSuiteResultsGridCell(SuiteResult suiteResult, final List<SuiteHistory> history, final int suiteResultsCellPerRow){
        Collections.sort(history, new Comparator<SuiteHistory>() {
            public int compare(SuiteHistory s1, SuiteHistory s2) {
                return s1.compareTo(s2);
            }
        });

        final int clientWidth = Window.getClientWidth();
        final int clientHeight = Window.getClientHeight();

        RowLayout rowLayout = new RowLayout(com.extjs.gxt.ui.client.Style.Orientation.VERTICAL);
        final ContentPanel contentPanel = new ContentPanel(rowLayout);
        String suiteNameBase = suiteResult.getCompoundKey().getSuiteName();
		String rawName = suiteNameBase.replace("_", " ").replace("-", " ");
		if(suiteResult.getJvmType() != null){
			rawName = NIGHTLY_REGRESSION.equals(suiteNameBase.toLowerCase()) ?
                    (Character.isDigit(suiteResult.getJvmType().charAt(0)) ?
					rawName + " " + suiteResult.getJvmType().substring(2).replace("_", " ")
                            : rawName + " " + suiteResult.getJvmType())
                    : rawName;
			rawName = CPP_REGRESSION.equals(suiteNameBase.toLowerCase()) ?
					rawName + " " + suiteResult.getJvmType() : rawName;
		}
        final String suiteName =  rawName;


        String name = suiteName.length() <= 22  && countUpperCaseLetters(suiteName) < 18 ? suiteName + "<br> </br>" : suiteName;
        contentPanel.setHeading(name);
        contentPanel.getHeader().setHeight("30px");
        contentPanel.setStyleName("suite-result");

        final int width = (clientWidth /2 / suiteResultsCellPerRow) - 10;
        contentPanel.setWidth(width);
        final int height = (clientHeight / 4) - 10;
        contentPanel.setHeight(height);

        int passed = suiteResult.getPassedTests();
        int total = suiteResult.getTotalTestsRun();
        int suspectedTests = suiteResult.getSuspectedTests();
        Image icon = new Image();

        com.extjs.gxt.ui.client.widget.Label statusLabel = new com.extjs.gxt.ui.client.widget.Label();
        statusLabel.setStyleName("suite-status");

        if(total == 0){
            statusLabel.setText("No Test Runs");
            statusLabel.addStyleName("red-text");
            icon.setUrl( IconsRepository.ICONS.thumbDown().getSafeUri() );
        }else{

            double successRate = ((double)passed / (total - suspectedTests));
            statusLabel.setText(percentage.format(successRate));
            if(successRate >= 0.98 && successRate < 1){
                contentPanel.addStyleName("orange-border");
                statusLabel.addStyleName("orange-text");
                icon.setUrl( IconsRepository.ICONS.thumbDown().getSafeUri() );
            }else{
                if(successRate < 0.98){
                    contentPanel.addStyleName("red-border");
                    statusLabel.addStyleName("red-text");
                    icon.setUrl( IconsRepository.ICONS.thumbDown().getSafeUri() );
                }else{
                    contentPanel.addStyleName("green-border");
                    statusLabel.addStyleName("green-text");
                    icon.setUrl( IconsRepository.ICONS.thumbUp().getSafeUri() );
                }
            }
        }

        long daysWithoutRun = 0;

        Date lastSuiteDate = dateFormatter.parse(suiteResult.getTimestamp());
        daysWithoutRun = getDeltaInDays(new Date(), lastSuiteDate);

        Image warningIcon = null;
        if(daysWithoutRun >= 2){
            warningIcon = new Image();
            contentPanel.addStyleName("orange-border");
            statusLabel.addStyleName("orange-text");
            warningIcon.setPixelSize(width/2, IconsRepository.ICONS.alarm().getHeight() * width/2 / IconsRepository.ICONS.alarm().getWidth());
            warningIcon.setUrl(IconsRepository.ICONS.alarm().getSafeUri());
        }


        String formattedDate = dateFormatterNoSec.format(lastSuiteDate);

        //add details of last run
        LayoutContainer detailsPanel = new LayoutContainer();
        detailsPanel.add(new Label(formattedDate));
        final String version = suiteResult.getCompoundKey().getBuildVersion() + " " + suiteResult.getCompoundKey().getMilestone();
        final String buildNumber = suiteResult.getCompoundKey().getBuildNumber();
        detailsPanel.add(new Label(version + " - " + buildNumber));


        final List<Widget> widgetList = new ArrayList<Widget>();
        widgetList.add(detailsPanel);

        //add passed/failed numbers
        HBoxLayout hBoxPassedLayout = new HBoxLayout();
        hBoxPassedLayout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
        hBoxPassedLayout.setPack(BoxLayout.BoxLayoutPack.START);
        LayoutContainer passedTestsPanel = new LayoutContainer();

        com.extjs.gxt.ui.client.widget.Label passedLabel = new com.extjs.gxt.ui.client.widget.Label(suiteResult.getPassedTests() +"");
        passedLabel.setTitle("# passed tests");
        passedLabel.addStyleName("green-text");

        int skippedTests = suiteResult.getType() != null && TGRID.equals(suiteResult.getType().toLowerCase()) ? suiteResult.getSkippedTests() : suiteResult.getSuspectedTests();
        com.extjs.gxt.ui.client.widget.Label skippedLabel = new com.extjs.gxt.ui.client.widget.Label(skippedTests +"");
        String skippedTitle = suiteResult.getType() != null && TGRID.equals(suiteResult.getType().toLowerCase()) ? "skipped" : "suspected";
        skippedLabel.setTitle("# " + skippedTitle + " tests");
        skippedLabel.addStyleName("orange-text");

        com.extjs.gxt.ui.client.widget.Label failedLabel = new com.extjs.gxt.ui.client.widget.Label(suiteResult.getFailedTests() +"");
        failedLabel.setTitle("# failed tests");
        failedLabel.addStyleName("red-text");

        com.extjs.gxt.ui.client.widget.Label totalLabel = new com.extjs.gxt.ui.client.widget.Label(suiteResult.getTotalTestsRun() +"");
        totalLabel.setTitle("# total tests");
        totalLabel.addStyleName("blue-text");

        HBoxLayoutData hBoxPassedLayoutData = new HBoxLayoutData(new Margins(0, 5, 0, 0));
        passedTestsPanel.add(passedLabel, hBoxPassedLayoutData);
        passedTestsPanel.add(new com.extjs.gxt.ui.client.widget.Label("|"), hBoxPassedLayoutData);
        passedTestsPanel.add(failedLabel, hBoxPassedLayoutData);
        passedTestsPanel.add(new com.extjs.gxt.ui.client.widget.Label("|"), hBoxPassedLayoutData);
        passedTestsPanel.add(skippedLabel, hBoxPassedLayoutData);
        passedTestsPanel.add(new com.extjs.gxt.ui.client.widget.Label("|"), hBoxPassedLayoutData);
        passedTestsPanel.add(totalLabel, new HBoxLayoutData(new Margins(0)));

        widgetList.add(passedTestsPanel);

        //add status icons and percentage
        HBoxLayout hBoxStatusLayout = new HBoxLayout();
        hBoxStatusLayout.setHBoxLayoutAlign(HBoxLayout.HBoxLayoutAlign.MIDDLE);
        hBoxStatusLayout.setPack(BoxLayout.BoxLayoutPack.START);
        LayoutContainer statusPanel = new LayoutContainer();

        statusPanel.setLayout(hBoxStatusLayout);
        HBoxLayoutData hBoxStatusLayoutData = new HBoxLayoutData(new Margins(0, 20, 0, 0));
        statusPanel.add(icon, hBoxStatusLayoutData);
        if(warningIcon != null){
            statusPanel.add(warningIcon, hBoxStatusLayoutData);
        }
        statusPanel.add(statusLabel, new HBoxLayoutData(new Margins(0)));
        widgetList.add(statusPanel);

        //add suite link
        Anchor link = new Anchor("Suite Report", true);
        link.setHref(suiteResult.getSuiteReportLink());
        link.setTarget("_blank");
        link.setTitle(SUITE_REPORT_LINK);
        link.addStyleName("link");

        widgetList.add(link);

        if(daysWithoutRun >= 2){
            widgetList.remove(passedTestsPanel);
            statusPanel.remove(statusLabel);
            statusPanel.remove(icon);
            widgetList.remove(link);

        }

        //add components to main widget
        addAllWidgets(contentPanel, widgetList);

        //add listener to display th chart
        contentPanel.addListener(Events.OnClick, new Listener<BaseEvent>() {
            @Override
            public void handleEvent(BaseEvent baseEvent) {
                chart = createChart(width * 2, history);
                final Dialog simple = new Dialog();
                //simple.setBodyStyleName("chart-dialog");
                simple.setStyleName("chart-dialog");
                simple.setAutoWidth(true);
                simple.setButtons(Dialog.OK);
                simple.setHeading(version + " - " + suiteName + " History");
                simple.add(chart);
                simple.getItem(0).getFocusSupport().setIgnore(true);
                simple.setScrollMode(com.extjs.gxt.ui.client.Style.Scroll.AUTO);
                simple.setHideOnButtonClick(true);
                //this is for weird resizing problem
                simple.removeAllListeners();
                simple.show();
            }
        });

        return contentPanel;
    }

    private void addAllWidgets(ContentPanel contentPanel, List<Widget> widgetList) {
        for (Widget widget : widgetList) {
            contentPanel.add(widget);
        }
    }

    private Chart createChart(int width, final List<SuiteHistory> history){
        final Map<Integer, SuiteHistory> builds = new LinkedHashMap<Integer, SuiteHistory>();
        double min = Double.MAX_VALUE;


        final int resultsSize = history.size();
        int resultsCounter = 0;
        for(SuiteHistory suiteHistory : history){
            builds.put( resultsCounter++, suiteHistory);
            min = Math.min(min, (double) suiteHistory.getPassedTestsHistory() / suiteHistory.getTotalTestsHistory() * 100);
        }

        DoubleValueFormatter xAxisFormatter = new DoubleValueFormatter() {
            public String formatValue(double value) {
                int counter = (int)value;
                if(resultsSize > 5){
                    return ( counter % 2 == 0 ) ?  builds.get( counter ).getBuildNumber() : "";
                }else{
                    return builds.get( counter ).getBuildNumber();
                }
            }
        };


        GraphSeriesPointFormatter tooltipFormatter = new GraphSeriesPointFormatter() {
            @Override
            public String formatValue(String seriesName, double x, double y, String customData) {
                if (customData != null) {
                    return "<b>" + customData + "</b>";
                }
                return "";
            }
        };


        ChartOptions options = new ChartOptions().width(width).height(200).markerRadius(3)
                .toolTipFormatter(tooltipFormatter)
                .xAxisFormatter(xAxisFormatter)
                .minYValue(Math.max(0, (int)min - 2)).maxYValue(100)
                .colors("#4572A7", "#AA4643", "#89A54E", "#80699B", "#3D96AE", "#DB843D", "#92A8CD", "#A47D7C", "#B5CA92")
                .yAxisTickPixelInterval( 50 )
                //.xAxisType( AxisType.LINEAR )
                .xAxisLineColor("#6098BF")
                .xAxisGridLineColor("#98BCD5")
                .yAxisLineColor("#6098BF")
                .yAxisGridLineColor("#CEDFEB")
                .legendEnabled(false)
                .xAxisTickLabelStep(1)
                .xAxisTickInterval(1)
                .plotBorderWidth(1)
                .marginTop(1)
                .marginRight(5)
                .marginLeft(30)
                .marginBottom(32)
                .yAxisTickPixelInterval(20)
                .xAxisLabelFontColor("#5086ab")
                .xAxisLabelFontSize("10px")
                .xAxisLabelFontFamily("tahoma")
                .yAxisLabelFontColor("#6098bf")
                .yAxisLabelFontSize("8px")
                .yAxisLabelFontFamily("arial");

        final Chart chart = new Chart(options);
        final String seriesName = "tests_results";
        final SeriesOptions seriesOptions = new SeriesOptions().name(seriesName).type(SeriesType.SPLINE);

        chart.addAttachHandler( new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {
                try{
                    if( event.isAttached() ){

                        chart.addSeries(seriesOptions);

                        Set<Integer> keysMap = builds.keySet();

                        for(int resultsCounter : keysMap){
                            SuiteHistory suiteHistory = builds.get(resultsCounter);
                            double percentPassed = ((double)suiteHistory.getPassedTestsHistory()
                                    / (suiteHistory.getTotalTestsHistory() - suiteHistory.getSuspectedTestsHistory()) * 100);
                            String buildNumber = suiteHistory.getBuildNumber();
                            chart.addPoint( seriesName, new SeriesPoint().customProperty(buildNumber).x(resultsCounter).y(percentPassed), false);
                        }

                        chart.redraw();
                    }
                } catch (Throwable t){
                    t.printStackTrace();
                }
            }
        } );
        return chart;
    }

    public static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;

    static public long getDeltaInDays(Date latterDate, Date earlierDate) {
        long deltaInMillis = latterDate.getTime() - earlierDate.getTime();
        return deltaInMillis / MILLIS_PER_DAY;
    }

    static public int countUpperCaseLetters(String toCount){
        int upperCaseCount = 0;

        for (int i=0; i<toCount.length(); i++)
        {
            for(char c = 'A';  c <= 'Z'; c++)
            {
                if (toCount.charAt(i) == c)
                {
                    upperCaseCount++;
                }
            }
        }
        return upperCaseCount;
    }
}