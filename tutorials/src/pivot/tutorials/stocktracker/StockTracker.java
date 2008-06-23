/*
 * Copyright (c) 2008 VMware, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pivot.tutorials.stocktracker;

import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.Locale;
import java.util.ResourceBundle;

import pivot.collections.ArrayList;
import pivot.collections.List;
import pivot.collections.Sequence;
import pivot.serialization.CSVSerializer;
import pivot.util.concurrent.Task;
import pivot.util.concurrent.TaskListener;
import pivot.web.GetQuery;
import pivot.wtk.Alert;
import pivot.wtk.Application;
import pivot.wtk.ApplicationContext;
import pivot.wtk.Button;
import pivot.wtk.ButtonPressListener;
import pivot.wtk.Component;
import pivot.wtk.ComponentKeyListener;
import pivot.wtk.Container;
import pivot.wtk.Display;
import pivot.wtk.Form;
import pivot.wtk.Keyboard;
import pivot.wtk.Label;
import pivot.wtk.Span;
import pivot.wtk.TableView;
import pivot.wtk.TableViewSelectionListener;
import pivot.wtk.TaskAdapter;
import pivot.wtk.TextInput;
import pivot.wtk.TextInputTextListener;
import pivot.wtk.Window;
import pivot.wtkx.ComponentLoader;

public class StockTracker implements Application {
    private ArrayList<String> symbols = new ArrayList<String>();

    private Window window = null;
    private TableView stocksTableView = null;
    private TextInput symbolTextInput = null;
    private Button addSymbolButton = null;
    private Button removeSymbolsButton = null;
    private Label lastUpdateLabel = null;
    private Button yahooFinanceButton = null;
    private Container detailRootPane = null;
    private Label detailChangeLabel = null;

    private GetQuery getQuery = null;

    public static final String SERVICE_HOSTNAME = "download.finance.yahoo.com";
    public static final String SERVICE_PATH = "/d/quotes.csv";
    public static final long REFRESH_INTERVAL = 15000;
    public static final String YAHOO_FINANCE_HOME = "http://finance.yahoo.com";

    public StockTracker() {
        symbols.setComparator(new Comparator<String>() {
            public int compare(String s1, String s2) {
                return s1.compareTo(s2);
            }
        });

        symbols.add("EBAY");
        symbols.add("AAPL");
        symbols.add("MSFT");
        symbols.add("AMZN");
        symbols.add("GOOG");
        symbols.add("VMW");
    }

    public void startup() throws Exception {
        ApplicationContext.getInstance().setTitle("Stock Tracker Demo");

        ComponentLoader.initialize();
        ComponentLoader componentLoader = new ComponentLoader(Locale.getDefault());

        Component content = componentLoader.load("pivot/tutorials/stocktracker/stocktracker.wtkx",
            getClass().getName());

        stocksTableView = (TableView)componentLoader.getComponent("stocksTableView");
        stocksTableView.getTableViewSelectionListeners().add(new TableViewSelectionListener() {
            public void selectionChanged(TableView tableView) {
                refreshDetail();
            }
        });

        stocksTableView.getComponentKeyListeners().add(new ComponentKeyListener() {
            public void keyTyped(Component component, char character) {
            }

            public void keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.DELETE) {
                    removeSelectedSymbols();
                }
            }

            public void keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            }
        });

        symbolTextInput = (TextInput)componentLoader.getComponent("symbolTextInput");
        symbolTextInput.getTextInputTextListeners().add(new TextInputTextListener() {
            public void textChanged(TextInput textInput) {
                addSymbolButton.setEnabled(textInput.getCharacterCount() > 0);
            }
        });

        symbolTextInput.getComponentKeyListeners().add(new ComponentKeyListener() {
            public void keyTyped(Component component, char character) {
            }

            public void keyPressed(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
                if (keyCode == Keyboard.KeyCode.ENTER) {
                    addSymbol();
                }
            }

            public void keyReleased(Component component, int keyCode, Keyboard.KeyLocation keyLocation) {
            }
        });

        addSymbolButton = (Button)componentLoader.getComponent("addSymbolButton");
        addSymbolButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                addSymbol();
            }
        });

        removeSymbolsButton = (Button)componentLoader.getComponent("removeSymbolsButton");
        removeSymbolsButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                removeSelectedSymbols();
            }
        });

        lastUpdateLabel = (Label)componentLoader.getComponent("lastUpdateLabel");

        yahooFinanceButton = (Button)componentLoader.getComponent("yahooFinanceButton");
        yahooFinanceButton.getButtonPressListeners().add(new ButtonPressListener() {
            public void buttonPressed(Button button) {
                try {
                    ApplicationContext.getInstance().open(new URL(YAHOO_FINANCE_HOME));
                } catch(MalformedURLException exception) {
                }
            }
        });

        detailRootPane = (Container)componentLoader.getComponent("detail.rootPane");

        detailChangeLabel = (Label)componentLoader.getComponent("detail.changeLabel");

        window = new Window();
        window.setContent(content);
        window.getAttributes().put(Display.MAXIMIZED_ATTRIBUTE, Boolean.TRUE);
        window.open();

        refreshTable();

        ApplicationContext.setInterval(new Runnable() {
            public void run() {
                refreshTable();
            }
        }, REFRESH_INTERVAL);

        Component.setFocusedComponent(symbolTextInput);
    }

    public void shutdown() throws Exception {
        window.close();
    }

    public void suspend() throws Exception {
    }

    public void resume() throws Exception {
    }

    @SuppressWarnings("unchecked")
    private void refreshTable() {
        if (getQuery != null) {
            // TODO
            // getQuery.abort();
        }

        getQuery = new GetQuery(SERVICE_HOSTNAME, SERVICE_PATH);

        StringBuilder symbolsArgumentBuilder = new StringBuilder();
        for (int i = 0, n = symbols.getLength(); i < n; i++) {
            if (i > 0) {
                symbolsArgumentBuilder.append(",");
            }

            symbolsArgumentBuilder.append(symbols.get(i));
        }

        // Format:
        // s - symbol
        // n - company name
        // l1 - most recent value
        // o - opening value
        // h - high value
        // g - low value
        // c1 - change percentage
        // v - volume
        String symbolsArgument = symbolsArgumentBuilder.toString();
        getQuery.getArguments().put("s", symbolsArgument);
        getQuery.getArguments().put("f", "snl1ohgc1v");

        CSVSerializer quoteSerializer = new CSVSerializer();
        quoteSerializer.getKeys().add(StockQuote.SYMBOL_KEY);
        quoteSerializer.getKeys().add(StockQuote.COMPANY_NAME_KEY);
        quoteSerializer.getKeys().add(StockQuote.VALUE_KEY);
        quoteSerializer.getKeys().add(StockQuote.OPENING_VALUE_KEY);
        quoteSerializer.getKeys().add(StockQuote.HIGH_VALUE_KEY);
        quoteSerializer.getKeys().add(StockQuote.LOW_VALUE_KEY);
        quoteSerializer.getKeys().add(StockQuote.CHANGE_KEY);
        quoteSerializer.getKeys().add(StockQuote.VOLUME_KEY);

        quoteSerializer.setItemClass(StockQuote.class);
        getQuery.setSerializer(quoteSerializer);

        getQuery.execute(new TaskAdapter<Object>(new TaskListener<Object>() {
            public void taskExecuted(Task<Object> task) {
                if (task == getQuery) {
                    Sequence<Span> selectedRanges = stocksTableView.getSelectedRanges();

                    List<StockQuote> quotes = (List<StockQuote>)task.getResult();
                    stocksTableView.setTableData(quotes);

                    if (selectedRanges.getLength() > 0) {
                        stocksTableView.setSelectedRanges(selectedRanges);
                    } else {
                        if (quotes.getLength() > 0) {
                            stocksTableView.setSelectedIndex(0);
                        }
                    }

                    ResourceBundle resourceBundle = ResourceBundle.getBundle(StockTracker.class.getName());
                    String lastUpdateText = resourceBundle.getString("lastUpdate")
                        + ": " + DateFormat.getDateTimeInstance().format(new Date());
                    lastUpdateLabel.setText(lastUpdateText);

                    getQuery = null;
                }
            }

            public void executeFailed(Task<Object> task) {
                if (task == getQuery) {
                    System.out.println(task.getFault());
                    getQuery = null;
                }
            }
        }));
    }

    @SuppressWarnings("unchecked")
    private void refreshDetail() {
        int firstSelectedIndex = stocksTableView.getFirstSelectedIndex();
        removeSymbolsButton.setEnabled(firstSelectedIndex != -1);

        StockQuote stockQuote = null;
        detailChangeLabel.getAttributes().remove(Form.FLAG_ATTRIBUTE);

        if (firstSelectedIndex != -1) {
            int lastSelectedIndex = stocksTableView.getLastSelectedIndex();

            if (firstSelectedIndex == lastSelectedIndex) {
                List<StockQuote> tableData = (List<StockQuote>)stocksTableView.getTableData();
                stockQuote = tableData.get(firstSelectedIndex);

                if (stockQuote.getChange() < 0) {
                    detailChangeLabel.getAttributes().put(Form.FLAG_ATTRIBUTE,
                        new Form.Flag(Alert.Type.ERROR));
                }
            }
        }

        StockQuoteView stockQuoteView = new StockQuoteView(stockQuote);
        detailRootPane.load(stockQuoteView);
    }

    @SuppressWarnings("unchecked")
    private void addSymbol() {
        String symbol = symbolTextInput.getText().toUpperCase();
        if (symbols.indexOf(symbol) == -1) {
            int index = symbols.add(symbol);

            List<StockQuote> tableData = (List<StockQuote>)stocksTableView.getTableData();
            StockQuote stockQuote = new StockQuote();
            stockQuote.put(StockQuote.SYMBOL_KEY, symbol);
            tableData.insert(stockQuote, index);

            stocksTableView.setSelectedIndex(index);
        }

        symbolTextInput.setText("");
        refreshTable();
    }

    private void removeSelectedSymbols() {
        int selectedIndex = stocksTableView.getFirstSelectedIndex();
        int selectionLength = stocksTableView.getLastSelectedIndex() - selectedIndex + 1;
        stocksTableView.getTableData().remove(selectedIndex, selectionLength);
        symbols.remove(selectedIndex, selectionLength);

        if (selectedIndex >= symbols.getLength()) {
            selectedIndex = symbols.getLength() - 1;
        }

        stocksTableView.setSelectedIndex(selectedIndex);
    }
}
