package com.gigaspaces.quality.dashboard.client;

import com.gigaspaces.quality.dashboard.client.icons.IconsRepository;
import com.gigaspaces.quality.dashboard.shared.CompoundSuiteHistoryResult;
import com.gigaspaces.quality.dashboard.shared.SuiteHistory;
import com.gigaspaces.quality.dashboard.shared.SuiteResult;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.RepeatingCommand;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.Style.BorderStyle;
import com.google.gwt.dom.client.Style.FontWeight;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.logical.shared.AttachEvent;
import com.google.gwt.event.logical.shared.AttachEvent.Handler;
import com.google.gwt.event.logical.shared.SelectionEvent;
import com.google.gwt.event.logical.shared.SelectionHandler;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.*;
import com.google.gwt.user.client.ui.HasHorizontalAlignment.HorizontalAlignmentConstant;
import org.highchartsgwt.client.Chart;
import org.highchartsgwt.client.formatters.DoubleValueFormatter;
import org.highchartsgwt.client.formatters.GraphSeriesPointFormatter;
import org.highchartsgwt.client.options.ChartOptions;
import org.highchartsgwt.client.options.SeriesOptions;
import org.highchartsgwt.client.utils.AxisType;
import org.highchartsgwt.client.utils.SeriesPoint;
import org.highchartsgwt.client.utils.SeriesType;

import java.util.*;

/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Dashboard implements EntryPoint {
    private int curComponentsCountPerLastRow = 0;
    private HorizontalPanel _curRowPanel;
    private List<VerticalPanel> verticalPanels = new ArrayList<VerticalPanel>();
    private DashboardServiceAsync dashboardServiceAsyncService = GWT.create( DashboardService.class );
    private int selectedItem;

    public void onModuleLoad() {

        RootPanel.getBodyElement().getStyle().setBackgroundColor("black");
        final TabPanel mainPanel = new TabPanel();
        mainPanel.setSize( "100%", "100%");
        mainPanel.addSelectionHandler(new SelectionHandler<Integer>() {
            @Override
            public void onSelection(SelectionEvent<Integer> event) {
                selectedItem = event.getSelectedItem();

            }
        });
        RootPanel.get("resultGrid").add(mainPanel);

        refresh(mainPanel);

        Scheduler.get().scheduleFixedPeriod(new RepeatingCommand() {
            @Override
            public boolean execute() {
                return refresh(mainPanel);
            }
        },5 * 60 * 1000);


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
                for(String xapVersion : keysMap){
                    VerticalPanel panel = new VerticalPanel();
                    verticalPanels.add(panel);
                    for(SuiteResult result : compoundSuiteHistoryResults.get(xapVersion).getResults()){
                        addSuiteResult(panel, result, compoundSuiteHistoryResults.get(xapVersion).getSuiteHistory().get(result.getCompoundKey().getSuiteName()));
                    }
                    mainPanel.add(panel, xapVersion);
                    _curRowPanel = null;
                    curComponentsCountPerLastRow = 0;
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

    public void addSuiteResult(VerticalPanel panel, SuiteResult suiteResult, List<SuiteHistory> history){
        int clientWidth = Window.getClientWidth();
        int suiteResultsCellWidth = 350;
        int suiteResultsCellPerRow = clientWidth/suiteResultsCellWidth;

        if( curComponentsCountPerLastRow == 0 || curComponentsCountPerLastRow == suiteResultsCellPerRow ){
            _curRowPanel = new HorizontalPanel();
            panel.add( _curRowPanel );
            curComponentsCountPerLastRow = 0;
        }
        Widget suiteResultCell = createSuiteResultsGridCell(suiteResult, history, suiteResultsCellPerRow);
        curComponentsCountPerLastRow++;
        _curRowPanel.add( suiteResultCell );
    }


    private Widget createSuiteResultsGridCell(SuiteResult suiteResult, List<SuiteHistory> history, int suiteResultsCellPerRow){
        Collections.sort(history, new Comparator<SuiteHistory>() {
            public int compare(SuiteHistory s1, SuiteHistory s2) {
                return s1.compareTo(s2);
            }
        });

        int clientWidth = Window.getClientWidth();

        HorizontalPanel suiteResultsCell = new HorizontalPanel();
        suiteResultsCell.setWidth((int)(clientWidth/suiteResultsCellPerRow-30) + "px");
        suiteResultsCell.setHeight("200px");
        suiteResultsCell.setSpacing(3);

        Image icon = new Image();

        Style style = suiteResultsCell.getElement().getStyle();
        style.setMarginRight(25, Unit.PX);
        style.setMarginBottom(25, Unit.PX);
        style.setBorderWidth(10, Unit.PX);
        style.setBorderStyle(BorderStyle.SOLID);
        style.setBackgroundColor("#F0F8FF");
        double passed = Double.valueOf(suiteResult.getPassedTests());
        double total = Double.valueOf(suiteResult.getTotalTestsRun());

        if(total == 0){
            style.setBorderColor("red");
            icon.setUrl( IconsRepository.ICONS.thumbDown().getURL() );
        }else{
            double successRate = (passed/total)*100;
            if(successRate >= 98 && successRate < 100){
                style.setBorderColor("orange");
                icon.setUrl( IconsRepository.ICONS.thumbDown().getURL() );
            }else{
                if(successRate < 98){
                    style.setBorderColor("red");
                    icon.setUrl( IconsRepository.ICONS.thumbDown().getURL() );
                }else{
                    style.setBorderColor("green");
                    icon.setUrl( IconsRepository.ICONS.thumbUp().getURL() );
                }
            }
        }
        int daysWithoutRun = 0;
        DateTimeFormat dateFormatter = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm:ss.S");
        Date lastSuiteDate = dateFormatter.parse(suiteResult.getTimestamp());
        daysWithoutRun = getDeltaIndays(new Date(), lastSuiteDate);

        Image warningIcon = null;
        if(daysWithoutRun >= 2){
            warningIcon = new Image();
            warningIcon.setUrl(IconsRepository.ICONS.warning().getURL());
        }

        dateFormatter = DateTimeFormat.getFormat("yyyy-MM-dd HH:mm");
        String formattedDate = dateFormatter.format(lastSuiteDate);

        VerticalPanel resultsCell = new VerticalPanel();
        suiteResultsCell.setHorizontalAlignment( HorizontalAlignmentConstant.startOf( com.google.gwt.i18n.client.HasDirection.Direction.LTR ) );
        suiteResultsCell.add(resultsCell);

        Anchor link = new Anchor("Suite Report", true);
        link.setHref(suiteResult.getSuiteReportLink());
        link.setTarget("_blank");

        Label suiteNameLabel = new Label(suiteResult.getCompoundKey().getSuiteName().replace("_", " ").replace("-", " "));
        suiteNameLabel.getElement().getStyle().setFontWeight(FontWeight.BOLD);

        resultsCell.add(suiteNameLabel);
        resultsCell.add(new Label(formattedDate));
        resultsCell.add(new Label(suiteResult.getCompoundKey().getBuildVersion() + " " + suiteResult.getCompoundKey().getMilestone()));
        resultsCell.add(new Label(suiteResult.getCompoundKey().getBuildNumber()));

        HorizontalPanel resultsPanel = new HorizontalPanel();

        Label passedLabel = new Label(suiteResult.getPassedTests() +"");
        Style passedStyle = passedLabel.getElement().getStyle();
        passedStyle.setColor("green");
        passedStyle.setFontWeight(FontWeight.BOLD);

        Label failedLabel = new Label(suiteResult.getFailedTests() +"");
        Style failedStyle = failedLabel.getElement().getStyle();
        failedStyle.setColor("red");
        failedStyle.setFontWeight(FontWeight.BOLD);

        Label totalLabel = new Label(suiteResult.getTotalTestsRun() +"");
        Style totalStyle = totalLabel.getElement().getStyle();
        totalStyle.setColor("blue");
        totalStyle.setFontWeight(FontWeight.BOLD);

        resultsPanel.add(passedLabel);
        resultsPanel.add(new Label( "|"));
        resultsPanel.add(failedLabel);
        resultsPanel.add(new Label( "|"));
        resultsPanel.add(totalLabel);


        resultsCell.add(resultsPanel);
        HorizontalPanel iconsPanel = new HorizontalPanel();
        iconsPanel.add(icon);
        resultsCell.add(iconsPanel);
        if(warningIcon != null){
            iconsPanel.add(warningIcon);
        }
        resultsCell.add(link);


        int chartWIdth = clientWidth/suiteResultsCellPerRow - 150;
        suiteResultsCell.setHorizontalAlignment( HorizontalAlignmentConstant.startOf( com.google.gwt.i18n.client.HasDirection.Direction.RTL ) );
        suiteResultsCell.add( createChart(chartWIdth ,history) );

        return suiteResultsCell;
    }

    private Chart createChart(int width, final List<SuiteHistory> history){
        final Map<Integer, SuiteHistory> builds = new LinkedHashMap<Integer, SuiteHistory>();
        int max = 0;


        final int resultsSize = history.size();
        int resultsCounter = 0;
        for(SuiteHistory suiteHistory : history){
            builds.put( resultsCounter, suiteHistory);
            max = Math.max(max, suiteHistory.getPassedTestsHistory());
            resultsCounter++;
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
                .minYValue(0).maxYValue(max)
                .colors("#4572A7", "#AA4643", "#89A54E", "#80699B", "#3D96AE", "#DB843D", "#92A8CD", "#A47D7C", "#B5CA92")
                .yAxisTickPixelInterval( 50 )
                .xAxisType( AxisType.LINEAR )
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
                .xAxisLabelFontColor( "#5086ab" )
                .xAxisLabelFontSize( "10px" )
                .xAxisLabelFontFamily( "tahoma" )
                .yAxisLabelFontColor( "#6098bf" )
                .yAxisLabelFontSize( "8px" )
                .yAxisLabelFontFamily( "arial" );

        final Chart chart = new Chart(options);
        final String seriesName = "tests_results";
        final SeriesOptions seriesOptions = new SeriesOptions().name(seriesName).type(SeriesType.SPLINE).lineWidth(1).color("green").markerRadius(14);

        chart.addAttachHandler( new Handler() {
            @Override
            public void onAttachOrDetach(AttachEvent event) {

                if( event.isAttached() ){
                    chart.addSeries(seriesOptions);
                    Set<Integer> keysMap = builds.keySet();
                    for(Integer resultsCounter : keysMap){
                        SuiteHistory suiteHistory = builds.get(resultsCounter);
                        chart.addPoint( seriesName, new SeriesPoint().customProperty(suiteHistory.getBuildNumber() ).x(resultsCounter).y(suiteHistory.getPassedTestsHistory()), false);
                    }
                    chart.redraw();
                }
            }
        } );
        return chart;
    }

    public static final long MILLIS_PER_DAY = 24L * 60L * 60L * 1000L;

    static public int getDeltaIndays(Date latterDate, Date earlierDate) {
        long deltaInMillis = latterDate.getTime() - earlierDate.getTime();
        int deltaInWeeks = (int)(deltaInMillis / MILLIS_PER_DAY);
        return deltaInWeeks;
    }

}