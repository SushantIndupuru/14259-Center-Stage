package org.firstinspires.ftc.teamcode.opmode.tests;

import com.acmerobotics.roadrunner.Pose2d;
import com.acmerobotics.roadrunner.Rotation2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.IMU;

import org.firstinspires.ftc.teamcode.hardware.Deposit.DepoSlides;
import org.firstinspires.ftc.teamcode.hardware.Deposit.Pitch;
import org.firstinspires.ftc.teamcode.hardware.Sensors.Imu;
import org.firstinspires.ftc.teamcode.usefuls.Motor.DcMotorBetter;
import org.firstinspires.ftc.teamcode.drive.MecanumDrive;


public class CalculateDeposit extends LinearOpMode {

    private Rotation2d robotAngle;
    private double robotX;
    private double robotY;
    private double currentDepositExtension;
    private double currentPitchAngle;
    private Imu imu;

    private DepoSlides slides;

    private Pitch pitch;

    private MecanumDrive drive;

    public void calculateDepositExtensionAndPitchAngle(Rotation2d robotAngle, double robotX, double robotY, double currentDepositExtension, double currentPitchAngle) {

        /*
        robotX is in inches
        robotY is in inches
        assume robot angle is 0-360 degrees or 0-2pi radians (you choose),


        current deposit extension is 0-1(linear interpolated in reference to max extension and min extension where 1 is max and 0 is min) or in inches,


        current pitch angle is 0-360 degrees or 0-2pi radians (you choose),
        The pitch angle is a bit tricky, lets do fully flat is 0 degrees, and fully vertical is 90 degrees to keep it consistent; the backboard would be 60 degrees.
         */

        drive.updatePoseEstimate();
        this.robotAngle = drive.pose.heading;
        this.robotX = drive.pose.position.x;
        this.robotY = drive.pose.position.y;

        slides.update();
        pitch.update();
        this.currentDepositExtension = slides.getCurrentInches();
        this.currentPitchAngle = pitch.getCurrentRadians();

    }

    @Override
    public void runOpMode() throws InterruptedException {
        drive = new MecanumDrive(hardwareMap, new Pose2d(0, 0, 0));

        pitch = new Pitch(new DcMotorBetter(hardwareMap.get(DcMotorEx.class, "pitchMotor")));
        slides = new DepoSlides(new DcMotorBetter(hardwareMap.get(DcMotorEx.class,"leftSlides")), new DcMotorBetter(hardwareMap.get(DcMotorEx.class,"rightSlides")));


        imu = new Imu(hardwareMap.get(IMU.class, "imu"));
        imu.init();

        waitForStart();
        while(opModeIsActive()){

            calculateDepositExtensionAndPitchAngle(robotAngle, robotX, robotY, currentDepositExtension, currentPitchAngle);

        }
    }
}