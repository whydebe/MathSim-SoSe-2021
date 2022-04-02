package app;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.Timer;

import utils.ApplicationTime;
import utils.FrameUpdate;
import javax.swing.*;

public class Simulation {

	private static JFrame frame;

	public static void main(String[] args) {
		ApplicationTime animThread = new ApplicationTime();
		animThread.start();

		createFrame(animThread);
		
		Timer timer = new Timer();
		timer.scheduleAtFixedRate(new FrameUpdate(frame), 100, _0_Constants.TPF);
	}
	
	private static void createFrame(ApplicationTime thread) {
		frame = new JFrame("Simulation: Billard - Gruppe 2.2");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new YourGraphicsContent(thread);
		frame.add(panel);
		frame.pack();
		frame.setVisible(true);
	}
}

@SuppressWarnings("serial") 
class YourGraphicsContent extends JPanel {
	
	// Panel has a single time tracking thread associated with it
	private ApplicationTime t;
	private double time;
	
	public YourGraphicsContent(ApplicationTime thread) {
		this.t = thread;
	}

	public Dimension getPreferredSize() {
		return new Dimension(_0_Constants.WINDOW_WIDTH, _0_Constants.WINDOW_HEIGHT);
	}

    public double[] output_onscreen_collision = new double[3];      // Distance (0), C_Border (1), C_Ball (2)
    public double[] output_onscreen_x_position = new double[4];
    public double[] output_onscreen_V_all = new double[2];          // Ball 1: [V_all_1 (0) | V_all_2 (1)]   |   Ball_2: [V_all_1 (2), V_all_2 (3)]
    public double[] output_onscreen_u_center_mass = new double[4];  // Ball  : [V_cm_1  (0) | V_cm_1  (1)]
    public double[] output_onscreen_u_parallel = new double[4];     // Ball 1: [u_par_1 (0) | u_par_2 (1)]   |   Ball_2: [u_par_1 (2), u_par_2 (3)]
    public double[] output_onscreen_u_orthogonal = new double[4];   // Ball 1: [u_ort_1 (0) | u_ort_2 (1)]   |   Ball_2: [u_ort_1 (2), u_ort_2 (3)]

	public class Ball {
        double[] x_position_temporary = new double[2];

        double[] x_position = new double[2];
        double[] v_velocity = new double[2];
        double[] u_center_mass = new double[2];
        double[] u_parallel = new double[2];
        double[] u_orthogonal = new double[2];
        double[] u_temporary = new double[2];
        double[] b_vector = new double[2];
        double[] V_velocity_all = new double[2];
        double diameter;
        double mass;
        double damp;
        double distance;

		public Ball(double x_position_X, double x_position_Y, double v_velocity_X, double v_velocity_Y, double diameter, double mass, double damp) {
            this.x_position[0] = x_position_X;
            this.x_position[1] = x_position_Y;
            this.v_velocity[0] = v_velocity_X;
            this.v_velocity[1] = v_velocity_Y;
            this.diameter = diameter;
            this.mass = mass;
            this.damp = damp;
		}

		public void Collision_Border() {

            this.x_position_temporary[0] = this.x_position[0] + this.v_velocity[0] / _0_Constants.FPS;
            this.x_position_temporary[1] = this.x_position[1] + this.v_velocity[1] / _0_Constants.FPS;

            if (this.x_position_temporary[0] > width - this.diameter || this.x_position[0] < 0) {
                this.v_velocity[0] *= -1;
                output_onscreen_collision[1]++;
            }

            if (this.x_position_temporary[1] > height - this.diameter || this.x_position[1] < 0) {
                this.v_velocity[1] *= -1;
                output_onscreen_collision[1]++;
            }
		}

        public double Distance(double x2_coordinate_X, double x2_coordinate_Y) {
            double x1_coordinate_X = this.x_position[0] + this.diameter/2;
            double x1_coordinate_Y = this.x_position[1] + this.diameter/2;
            b_vector[0] = x2_coordinate_X - x1_coordinate_X;
            b_vector[1] = x2_coordinate_Y - x1_coordinate_Y;
            double distance_X = Math.pow(b_vector[0], 2);
            double distance_Y = Math.pow(b_vector[1], 2);

            distance = Math.sqrt(distance_X + distance_Y);
            output_onscreen_collision[0] = distance;
            return distance;
        }

        public void Collision_Ball() {
            
            for (Ball ball : balls) {
                if (this == ball) {
                    continue;
                } 
                
                if (Distance(ball.x_position[0] + ball.diameter/2, ball.x_position[1] + ball.diameter/2) <= this.diameter/2 + ball.diameter/2) {
                    
                    this.V_velocity_all[0] = (this.mass * this.v_velocity[0] + ball.mass * ball.v_velocity[0]) / (this.mass + ball.mass);
                    this.V_velocity_all[1] = (this.mass * this.v_velocity[1] + ball.mass * ball.v_velocity[1]) / (this.mass + ball.mass);

                   
                    if (Distance(ball.x_position[0] + ball.diameter/2, ball.x_position[1] + ball.diameter/2) <= this.diameter/2 + ball.diameter/2) {
                        output_onscreen_collision[2]++;
                    }

                    System.out.println("Ball 1 Position (x): ( " + String.format("%.02f", ball_1.x_position[0]) + " | " + String.format("%.02f", ball_1.x_position[1]) + " )");
                    System.out.println("Ball 2 Position (x): ( " + String.format("%.02f", ball_2.x_position[0]) + " | " + String.format("%.02f", ball_2.x_position[1]) + " )\n");

                    // Calculate_U

                    // Ball1
                    this.u_center_mass[0] = (ball.mass/(this.mass + ball.mass)) * (this.v_velocity[0] - ball.v_velocity[0]);
                    this.u_center_mass[1] = (ball.mass/(this.mass + ball.mass)) * (this.v_velocity[1] - ball.v_velocity[1]);

                    double u_parallel_bruch = (this.u_center_mass[0] * b_vector[0] + this.u_center_mass[1] * b_vector[1]) / (b_vector[0] * b_vector[0] + b_vector[1] * b_vector[1]);
                    this.u_parallel[0] = u_parallel_bruch * b_vector[0];
                    this.u_parallel[1] = u_parallel_bruch * b_vector[1];

                    this.u_orthogonal[0] = this.u_center_mass[0] - this.u_parallel[0];
                    this.u_orthogonal[1] = this.u_center_mass[1] - this.u_parallel[1];

                    // Ball2
                    ball.u_center_mass[0] = (this.mass/(this.mass + ball.mass)) * (ball.v_velocity[0] - this.v_velocity[0]);
                    ball.u_center_mass[1] = (this.mass/(this.mass + ball.mass)) * (ball.v_velocity[1] - this.v_velocity[1]);

                    ball.u_parallel[0] = ((ball.u_center_mass[0] * b_vector[0] + ball.u_center_mass[1] * b_vector[1]) / (b_vector[0] * b_vector[0] + b_vector[1] * b_vector[1])) * b_vector[0];
                    ball.u_parallel[1] = ((ball.u_center_mass[0] * b_vector[0] + ball.u_center_mass[1] * b_vector[1]) / (b_vector[0] * b_vector[0] + b_vector[1] * b_vector[1])) * b_vector[1];

                    ball.u_orthogonal[0] = ball.u_center_mass[0] - ball.u_parallel[0];
                    ball.u_orthogonal[1] = ball.u_center_mass[1] - ball.u_parallel[1];
                        

                    // New U1_Vector
                    this.u_temporary[0] = (-1 * this.damp * this.u_parallel[0]) + this.u_orthogonal[0];
                    this.u_temporary[1] = (-1 * this.damp * this.u_parallel[1]) + this.u_orthogonal[1];

                    // New U2_Vector
                    ball.u_temporary[0] = (-1 * ball.damp * ball.u_parallel[0]) + ball.u_orthogonal[0];
                    ball.u_temporary[1] = (-1 * ball.damp * ball.u_parallel[1]) + ball.u_orthogonal[1];


                    // Need mmooooaaarrreeeeeee SPEEEEEEEEEEEED - just a joke xD
                    this.v_velocity[0] = this.u_temporary[0] + this.V_velocity_all[0];
                    this.v_velocity[1] = this.u_temporary[1] + this.V_velocity_all[1];

                    ball.v_velocity[0] = ball.u_temporary[0] + this.V_velocity_all[0];
                    ball.v_velocity[1] = ball.u_temporary[1] + this.V_velocity_all[1];
                } 
            }
        }
	}
	
	int width = _0_Constants.WINDOW_WIDTH;
	int height = _0_Constants.WINDOW_HEIGHT;

    // ---- TESTFÄLLE:  Erstellung der beiden Bälle in verschiedenen Szenarien -------------------
    // x_position_X, x_position_Y, v_velocity_X, v_velocity_Y, diameter, mass, damp


    // -- FALL 1 FRONTALE KOLLISION -------------------------------------------
    Ball ball_1 = new Ball(150, 250, 25, 0, 100, 1, 1);
    Ball ball_2 = new Ball(450, 250, -25, 0, 100, 1, 1);

    // -- FALL 1 FRONTALE KOLLISION - U. Geschwindigkeit ----------------------
    //Ball ball_1 = new Ball(230, 250, 5, 0, 100, 1, 1);
    //Ball ball_2 = new Ball(450, 250, -25, 0, 100, 1, 1);

    // -- FALL 1 FRONTALE KOLLISION - U. Masse und Durchmesser ----------------
    //Ball ball_1 = new Ball(200, 275, 25, 0, 50, 4, 1);
    //Ball ball_2 = new Ball(450, 250, -25, 0, 100, 1, 1);

    // -- FALL 1 FRONTALE KOLLISION - U. Dämpfungsfaktor ----------------------
    //Ball ball_1 = new Ball(150, 250, 25, 0, 100, 1, 0.3);
    //Ball ball_2 = new Ball(450, 250, -25, 0, 100, 1, 0.3);




    // -- FALL 2 ORTHOGONALE KOLLISION ----------------------------------------
    //Ball ball_1 = new Ball(250, 150, 0, 25, 100, 1, 1);
	//Ball ball_2 = new Ball(450, 350, -25, 0, 100, 1, 1);

    // -- FALL 2 ORTHOGONALE KOLLISION - U. Geschwindigkeit -------------------
    //Ball ball_1 = new Ball(250, 257, 0, 5, 100, 1, 1);
    //Ball ball_2 = new Ball(550, 350, -25, 0, 100, 1, 1);
    
    // -- FALL 2 ORTHOGONALE KOLLISION - U. Masse und Durchmesser -------------
    //Ball ball_1 = new Ball(250, 75, 0, 25, 50, 4, 1);
    //Ball ball_2 = new Ball(550, 350, -25, 0, 100, 1, 1);




    // -- FALL 3 VERSETZTE KOLLISION ------------------------------------------
    //Ball ball_1 = new Ball(152, 205, 25, 0, 100, 1, 1);
    //Ball ball_2 = new Ball(550, 295, -25, 0, 100, 1, 1);

    // -- FALL 3 VERSETZTE KOLLISION - U. Masse und Durchmesser ---------------
    //Ball ball_1 = new Ball(225, 275, 25, 0, 50, 4, 1);
    //Ball ball_2 = new Ball(610, 295, -25, 0, 100, 1, 1);



    // -- TEST ---------------------------------------------------------------
    //Ball ball_1 = new Ball(0, 0, 500/10, 450/10, 100, 1, 1);
    //Ball ball_2 = new Ball(550, 300/10, -500/10, 0, 100, 1, 1);
    
    //Ball ball_1 = new Ball(0, 0, 500, 450, 100, 1, 0.5);
    //Ball ball_2 = new Ball(550, 300, -500, 0, 100, 1, 1);

    //Ball ball_1 = new Ball(0, 0, 500, 850, 100, 1, 1);
    //Ball ball_2 = new Ball(550, 400, -500, 0, 100, 1, 1);


    Ball[] balls = {ball_1, ball_2};

	@Override protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		time = t.getTimeInSeconds();

		g.setColor(Color.DARK_GRAY);
		g.fillRect( 0, 0, width, height);
		g.setColor(Color.RED);

        for (Ball ball: balls) {
            for (int i = 0; i < 2; i++) {
                ball.x_position[i] += ball.v_velocity[i] / _0_Constants.FPS;
            }
            ball.Collision_Border();
            g.fillOval((int)ball.x_position[0], (int)ball.x_position[1], (int)ball.diameter, (int)ball.diameter);
        }

        ball_1.Collision_Ball();

        double[] position = new double[2];
        position[0] = ((ball_1.x_position[0] + ball_1.diameter/4 + ball_2.x_position[0] + ball_2.diameter/4)) / 2;
        position[1] = ((ball_1.x_position[1] + ball_2.diameter/4 + ball_2.x_position[1] + ball_2.diameter/4)) / 2;

        double[] cm_position_output = new double[2];
        cm_position_output[0] = ((ball_1.mass * ball_1.x_position[0] + ball_1.diameter/2) + (ball_2.mass * ball_2.x_position[0] + ball_2.diameter/2)) / (ball_1.mass + ball_2.mass);
        cm_position_output[1] = ((ball_1.mass * ball_1.x_position[1] + ball_1.diameter/2) + (ball_2.mass * ball_2.x_position[1] + ball_2.diameter/2)) / (ball_1.mass + ball_2.mass);

        g.setColor(Color.ORANGE);
        g.fillOval((int)cm_position_output[0], (int)cm_position_output[1], 25, 25);

        double[] cm_velocity_output = new double[2];
        cm_velocity_output[0] = (ball_1.mass * ball_1.v_velocity[0] + ball_2.mass * ball_2.v_velocity[0]) / (ball_1.mass + ball_2.mass);
        cm_velocity_output[1] = (ball_1.mass * ball_1.v_velocity[1] + ball_2.mass * ball_2.v_velocity[1]) / (ball_1.mass + ball_2.mass);
        g.drawString("CM Velocity: ( X: " + String.format("%.02f", cm_velocity_output[0]) + " | " + String.format("%.02f", cm_velocity_output[1]) + " :Y )", 320, 540);

        g.setColor(Color.WHITE);
        g.drawString("Collison: ( Border: " + (int) output_onscreen_collision[1] + " | " + (int) output_onscreen_collision[2] + " :Ball )", 320, 570);
        g.drawString("Distance: " + String.format("%.02f", output_onscreen_collision[0]), 320, 585);

        g.setColor(Color.CYAN);
        g.drawString("Ball 1:                   x               y", 15, 490);
        g.drawLine(15, 495, 185, 495);
        g.drawString("Position (x):  ( " + String.format("%.02f", ball_1.x_position[0]) + " | " + String.format("%.02f", ball_1.x_position[1]) + " )", 15, 510);
        g.drawString("Velocity (v):   ( " + String.format("%.02f", ball_1.v_velocity[0]) + " | " + String.format("%.02f", ball_1.v_velocity[1]) + " )", 15, 525);
        g.drawString("Diameter (d):   " + ball_1.diameter, 15, 555);
        g.drawString("Mass (m):         " + ball_1.mass, 15, 570);
        g.drawString("Damp (ε):         " + ball_1.damp, 15, 585);

        g.setColor(Color.GREEN);
        g.drawString("Ball 2:                   x               y", 615, 490);
        g.drawLine(615, 495, 785, 495);
        g.drawString("Position (x):  ( " + String.format("%.02f", ball_2.x_position[0]) + " | " + String.format("%.02f", ball_2.x_position[1]) + " )", 615, 510);
        g.drawString("Velocity (v):   ( " + String.format("%.02f", ball_2.v_velocity[0]) + " | " + String.format("%.02f", ball_2.v_velocity[1]) + " )", 615, 525);
        g.drawString("Diameter (d):   " + ball_2.diameter, 615, 555);
        g.drawString("Mass (m):         " + ball_2.mass, 615, 570);
        g.drawString("Damp (ε):         " + ball_2.damp, 615, 585);
	}
}