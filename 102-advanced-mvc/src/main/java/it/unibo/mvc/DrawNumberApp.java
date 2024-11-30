package it.unibo.mvc;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.lang.module.ModuleDescriptor.Builder;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;
import java.io.IOException;

/**
 */
public final class DrawNumberApp implements DrawNumberViewObserver {

    private final DrawNumber model;
    private final List<DrawNumberView> views;

    /**
     * @param views
     *            the views to attach
     */
    public DrawNumberApp(final String configurator, final DrawNumberView... views) {
        /*
         * Side-effect proof
         */
        this.views = Arrays.asList(Arrays.copyOf(views, views.length));
        for (final DrawNumberView view: views) {
            view.setObserver(this);
            view.start();
        }
        final Configuration.Builder builder = new Configuration.Builder();
        try (BufferedReader stream = new BufferedReader(new InputStreamReader(ClassLoader.getSystemResourceAsStream(configurator)))) {
            String line;
            while ((line = stream.readLine()) != null) {
                String [] temp = line.split(":");
                if (temp[0].contains("max")) {
                    builder.setMax(Integer.parseInt(temp[1].trim()));
                } else if (temp[0].contains("min")) {
                    builder.setMin(Integer.parseInt(temp[1].trim()));
                } else if (temp[0].contains("attempts")) {
                    builder.setAttempts(Integer.parseInt(temp[1].trim()));
                }  else {
                    throw new IllegalArgumentException("The file have unexpected string");
                }
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());;
        }
        final Configuration configuration = builder.build();
        if (configuration.isConsistent()) {
            this.model = new DrawNumberImpl(configuration);
        } else {

            this.model = new DrawNumberImpl(new Configuration.Builder().build());
        }
    }
    

    @Override
    public void newAttempt(final int n) {
        try {
            final DrawResult result = model.attempt(n);
            for (final DrawNumberView view: views) {
                view.result(result);
            }
        } catch (IllegalArgumentException e) {
            for (final DrawNumberView view: views) {
                view.numberIncorrect();
            }
        }
    }

    @Override
    public void resetGame() {
        this.model.reset();
    }

    @Override
    public void quit() {
        /*
         * A bit harsh. A good application should configure the graphics to exit by
         * natural termination when closing is hit. To do things more cleanly, attention
         * should be paid to alive threads, as the application would continue to persist
         * until the last thread terminates.
         */
        System.exit(0);
    }

    /**
     * @param args
     *            ignored
     * @throws FileNotFoundException 
     */
    public static void main(final String... args) throws FileNotFoundException {
        new DrawNumberApp("config.yml", new DrawNumberViewImpl(), new PrintStreamView(System.out));
    }

}
