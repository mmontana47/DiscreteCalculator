package pl.edu.uj.discretecalculator.view.layout;

import javafx.geometry.Point2D;
import pl.edu.uj.discretecalculator.controller.CanvasManager;
import pl.edu.uj.discretecalculator.view.EdgeDrawn;
import pl.edu.uj.discretecalculator.view.StyleSettings;
import pl.edu.uj.discretecalculator.view.VertexDrawn;

import java.util.Random;

public class ForceDirectedLayout {
    private final CanvasManager canvas;
    private double IDEAL_LENGTH;
    private double K_GRAVITY;
    private static final double EPS = 0.01;
    private static final double DAMPING = 0.5;
    private final Random rng = new Random();

    private double liveTemperature = 0.0;
    public static final double MIN_TEMP = 0.9;
    public static final double KICK_TEMP = 25.0;
    public static final double COOL_FACTOR = 0.97;

    public ForceDirectedLayout(CanvasManager canvas) {
        this.canvas = canvas;
        recomputeParams();
    }

    public void recomputeParams() {
        int n = Math.max(8, canvas.getVertices().size());
        int m = canvas.getEdges().size();
        double maxEdges = n * (n - 1) / 2.0;
        double density = Math.clamp(m / maxEdges, 0.0, 1.0);
        double kScale  = 0.4 + 1.8 * Math.pow(density, 0.9);
        K_GRAVITY = -0.1 - 0.8 * (1.0 - density);
        double AREA = canvas.getGraphPane().getWidth() * canvas.getGraphPane().getHeight();
        double maxIdeal = Math.min(canvas.getGraphPane().getWidth(), canvas.getGraphPane().getHeight()) / 4.0;
        IDEAL_LENGTH = kScale * Math.min(maxIdeal, Math.sqrt(AREA / n));
    }

    public void tick() {
        recomputeParams();
        liveTemperature = Math.max(MIN_TEMP, liveTemperature*COOL_FACTOR);
        iteration(liveTemperature);
    }

    public void kick() {
        recomputeParams();
        liveTemperature = Math.max(liveTemperature, KICK_TEMP);
    }
    private double force_Edges(double d){ return 0.5*d*d/IDEAL_LENGTH;}
    private double force_Vertices(double d){
        return d < 3*IDEAL_LENGTH ? IDEAL_LENGTH*IDEAL_LENGTH/d : 0;
    }

    private Point2D calcForce(VertexDrawn v, VertexDrawn u){
        Point2D delta = new Point2D(v.getLayoutX() - u.getLayoutX(), v.getLayoutY() - u.getLayoutY());
        double dist = delta.magnitude();
        Point2D direction;
        if(dist < EPS){
            direction = new Point2D(rng.nextDouble()-0.5, rng.nextDouble()-0.5).normalize();
            dist = EPS;
        }
        else direction = delta.normalize();
        return direction.multiply(force_Vertices(dist));
    }

    //Fruchterman–Reingold algorithm
    public void iteration(double temperature){
        Point2D center = new Point2D(canvas.getGraphPane().getWidth()/2, canvas.getGraphPane().getHeight()/2);
        for (VertexDrawn v : canvas.getVertices()) {
            v.resetDisplacement();
            for (VertexDrawn u : canvas.getVertices()) {
                if(u.equals(v)){continue;}
                v.addDisplacement(calcForce(v, u));
            }
            Point2D x = new Point2D(v.getLayoutX(), v.getLayoutY());
            v.addDisplacement((x.subtract(center)).multiply(K_GRAVITY));
        }

        for(EdgeDrawn e : canvas.getEdges()){
            VertexDrawn v = e.getSource();
            VertexDrawn u = e.getTarget();
            Point2D delta = new Point2D(v.getLayoutX() - u.getLayoutX(), v.getLayoutY() - u.getLayoutY());
            double dist = delta.magnitude();
            if (dist < EPS) continue;
            Point2D normalize = delta.normalize();
            Point2D force = normalize.multiply(force_Edges(dist));
            v.removeDisplacement(force);
            u.addDisplacement(force);
        }

        for(VertexDrawn v : canvas.getVertices()){
            if (v.isPinned()) continue;
            double magnitude = v.getDisplacement().magnitude();
            Point2D normalize = v.getDisplacement().normalize();
            double step = Math.min(magnitude, temperature) * DAMPING;
            v.setLayoutX(v.getLayoutX() + normalize.getX()*step);
            v.setLayoutY(v.getLayoutY() + normalize.getY()*step);

            double r = StyleSettings.get().getVertexRadius();
            v.setLayoutX(Math.clamp(v.getLayoutX(), 0, canvas.getGraphPane().getWidth()-2*r));
            v.setLayoutY(Math.clamp(v.getLayoutY(), 0, canvas.getGraphPane().getHeight()-2*r));
        }
    }

    public void turbulence(){
        for (VertexDrawn v : canvas.getVertices()){
            v.setLayoutX(v.getLayoutX()-10+20*rng.nextDouble());
            v.setLayoutY(v.getLayoutY()-10+20*rng.nextDouble());
        }
    }
}