package org.firstinspires.ftc.teamcode.opmode.centerstage.autonomous;

import static org.firstinspires.ftc.teamcode.vision.CameraPipeline.color;
import static org.firstinspires.ftc.teamcode.vision.CameraPipeline.leftPer;
import static org.firstinspires.ftc.teamcode.vision.CameraPipeline.midPer;
import static org.firstinspires.ftc.teamcode.vision.CameraPipeline.rightPer;

import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.teamcode.drive.posePID2.DT;
import org.firstinspires.ftc.teamcode.hardware.Deposit.Claw;
import org.firstinspires.ftc.teamcode.hardware.Deposit.DepoArm;
import org.firstinspires.ftc.teamcode.hardware.Deposit.DepoSlides;
import org.firstinspires.ftc.teamcode.hardware.Deposit.Pitch;
import org.firstinspires.ftc.teamcode.hardware.Deposit.Wrist;
import org.firstinspires.ftc.teamcode.hardware.Intake.LTIntake;
import org.firstinspires.ftc.teamcode.usefuls.Motor.DcMotorBetter;
import org.firstinspires.ftc.teamcode.usefuls.Motor.ServoMotorBetter;
import org.firstinspires.ftc.teamcode.vision.AprilTagPipeline;
import org.firstinspires.ftc.teamcode.vision.CameraPipeline;
import org.firstinspires.ftc.vision.VisionPortal;
import org.firstinspires.ftc.vision.apriltag.AprilTagDetection;
import org.firstinspires.ftc.vision.apriltag.AprilTagProcessor;
import org.openftc.easyopencv.OpenCvCamera;
import org.openftc.easyopencv.OpenCvCameraRotation;
import org.openftc.easyopencv.OpenCvWebcam;

@Config
@Autonomous
public class RegionalsBlue extends LinearOpMode {
    boolean timeToggle = true;
    double TimeStamp = 0;
    ElapsedTime timer = new ElapsedTime();
    Claw claw;
    DepoSlides slides;
    DT drive;
    Pose2d startBPose = new Pose2d(19, 65, Math.toRadians(90)); //default position
    DcMotorEx intakeMotor;
    LTIntake intake;
    Servo intakeArm2;
    Servo intakeArm1;
    Servo arm;
    Servo wrist;
    DepoArm arm1;
    Wrist wrist1;

    int currentState = 0;
    Pitch pitch;

    //vision
    OpenCvWebcam webcam;
    public String ObjectDirection;
    public int randomization = 99;
    public double thresh = CameraPipeline.perThreshold;

    //apriltag
    private final AprilTagPipeline ac = new AprilTagPipeline();
    private VisionPortal visionPortal;
    private AprilTagProcessor aprilTag;
    private AprilTagDetection detection;

    @Override
    public void runOpMode() throws InterruptedException {
        intakeArm1 = hardwareMap.get(Servo.class, "intakeArm");
        intakeArm2 = hardwareMap.get(Servo.class, "intakeArm2");
        intakeMotor = hardwareMap.get(DcMotorEx.class, "Intake");
        intake = new LTIntake(intakeMotor, new ServoMotorBetter(intakeArm2), new ServoMotorBetter(intakeArm1));
        wrist = hardwareMap.get(Servo.class, "wrist");
        arm = hardwareMap.get(Servo.class, "arm");
        wrist1 = new Wrist(new ServoMotorBetter(wrist));
        arm1 = new DepoArm(new ServoMotorBetter(arm), new ServoMotorBetter(hardwareMap.get(Servo.class, "fake")));
        wrist1.setState(Wrist.WristState.INITIALIZE);
        arm1.setState(DepoArm.DepoArmState.INITIALIZE);
        arm1.update();
        wrist1.update();
        claw = new Claw(new ServoMotorBetter(hardwareMap.get(Servo.class, "claw")));
        claw.setState(Claw.ClawState.LATCHED);
        claw.update();
        intake.setState(LTIntake.IntakeState.INITIALIZE);
        intake.update();
        drive = new DT(hardwareMap, new Pose2d(startBPose.getX(), startBPose.getY(), startBPose.getHeading()));
        pitch = new Pitch(new DcMotorBetter(hardwareMap.get(DcMotorEx.class, "Pitch")));
        slides = new DepoSlides(new DcMotorBetter(hardwareMap.get(DcMotorEx.class,"leftSlides")), new DcMotorBetter(hardwareMap.get(DcMotorEx.class,"rightSlides")));
//        pitch.setState(Pitch.PitchState.INITIALIZE);
//        pitch.update();

        CameraPipeline.setColor("BLUE");
        telemetry.update();
        webcam = CameraPipeline.initPipeline(hardwareMap, telemetry);
        webcam.openCameraDeviceAsync(new OpenCvCamera.AsyncCameraOpenListener() {
            @Override
            public void onOpened() { webcam.startStreaming(320, 240, OpenCvCameraRotation.UPRIGHT); }
            @Override
            public void onError(int errorCode) {}
        });

        while(opModeInInit() && !isStopRequested()){
            ObjectDirection = CameraPipeline.randomization(thresh);
            randomization = CameraPipeline.PosToNum(ObjectDirection);

            telemetry.addData("Location:", ObjectDirection);
            telemetry.addData("Color:", color);
            telemetry.update();
        }

        waitForStart();
        webcam.stopRecordingPipeline();
        webcam.closeCameraDevice();

        WebcamName webcamName = hardwareMap.get(WebcamName.class, "Webcam 1");
        aprilTag = AprilTagPipeline.initAprilTag();
        visionPortal = AprilTagPipeline.initVision(webcamName, aprilTag);
        visionPortal.setProcessorEnabled(aprilTag, true);
        telemetry.update();
        int aprilTagNum = randomization + 1;

        while(opModeIsActive()){
            //left (farthest to the center of the feild)
            if(randomization == 2){
                if(currentState == 0){
                    drive.setMaxPower(.7);
                    drive.lineTo(50, 45, Math.toRadians(180));
                    if(drive.isAtTarget()){
                        currentState++;
                    }
                }else if(currentState == 1){
                    pitch.setState(Pitch.PitchState.AUTON_PRELOAD);
                    if(Math.abs(pitch.getCurrentPosition() - pitch.target) < .05){
                        currentState++;
                    }
                }else if(currentState == 2){
                    arm1.setState(DepoArm.DepoArmState.LT_SCORE);
                    wrist1.setState(Wrist.WristState.LT_SCORE);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 500){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 3){
                    slides.setState(DepoSlides.DepositState.AUTO_PRELOAD_SCORE);
//                    intake.setState(LTIntake.IntakeState.INTAKE_TELE);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 1000){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 4){
                    claw.setState(Claw.ClawState.UNLATCHED);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 750){
                        currentState++;
                        timeToggle = true;
                    }
                }else if (currentState == 5){
                    drive.setMaxPower(.2);
                    drive.lineTo(47, 45, Math.toRadians(180));
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 1500){
                        currentState++;
                        timeToggle = true;
                    }
                }else if (currentState == 6){
                    drive.setMaxPower(.5);
                    drive.lineTo(34,37,Math.toRadians(180));
                    if(drive.isAtTarget()){
                        currentState++;
                    }
                }else if(currentState == 7){
                    intake.setPower(.35);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 2000){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 8){
                    intake.setPower(0);
                    slides.setState(DepoSlides.DepositState.DOWN);
                    arm1.setState(DepoArm.DepoArmState.INTERMEDIATE);
                    wrist1.setState(Wrist.WristState.ABOVE_TRANSFER);
                    drive.setMaxPower(.5);
                    drive.lineTo(45,37,Math.toRadians(180));
                    if(drive.isAtTarget()){
                        currentState++;
                    }
                }else if(currentState == 9){
                    drive.lineTo(50, 60,Math.toRadians(180));
                    intake.setState(LTIntake.IntakeState.INTAKE_TELE);
                    if(drive.isAtTarget()){
                        currentState++;
                    }
                }else if(currentState == 10){ //retraction sequence
                    slides.setState(DepoSlides.DepositState.DOWN);
                    pitch.setState(Pitch.PitchState.INITIALIZE);
                    arm1.setState(DepoArm.DepoArmState.INTERMEDIATE);
                    wrist1.setState(Wrist.WristState.ABOVE_TRANSFER);
                    if(Math.abs(pitch.getCurrentPosition() - pitch.target) < .05){
                        if(timeToggle){//timeToggle starts at true by default
                            TimeStamp = timer.milliseconds();
                            timeToggle = false;
                        }
                        if(timer.milliseconds() > TimeStamp + 500){
                            currentState++;
                            timeToggle = true;
                        }
                    }
                }else if(currentState == 10){
                    arm1.setState(DepoArm.DepoArmState.ABOVE_TRANSFER);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 500){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 11){
                    arm1.setState(DepoArm.DepoArmState.TRANSFER);
                    wrist1.setState(Wrist.WristState.TRANSFER); //when new guide gets added
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 500){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 12){
                    wrist1.setState(Wrist.WristState.TRANSFER);
                    slides.setState(DepoSlides.DepositState.OVER_IN);
                }






            }else if(randomization == 1){ //middle
                if(currentState == 0){
                    drive.setMaxPower(.7);
                    drive.lineTo(50, 37, Math.toRadians(180));
                    if(drive.isAtTarget()){
                        currentState++;
                    }
                }else if(currentState == 1){
                    pitch.setState(Pitch.PitchState.AUTON_PRELOAD);
                    if(Math.abs(pitch.getCurrentPosition() - pitch.target) < .05){
                        currentState++;
                    }
                }else if(currentState == 2){
                    arm1.setState(DepoArm.DepoArmState.LT_SCORE);
                    wrist1.setState(Wrist.WristState.LT_SCORE);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 500){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 3){
                    slides.setState(DepoSlides.DepositState.AUTO_PRELOAD_SCORE);
//                    intake.setState(LTIntake.IntakeState.INTAKE_TELE);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 1000){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 4){
                    claw.setState(Claw.ClawState.UNLATCHED);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 750){
                        currentState++;
                        timeToggle = true;
                    }
                }else if (currentState == 5){
                    drive.setMaxPower(.2);
                    drive.lineTo( 47, 37, Math.toRadians(180));
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 1500){
                        currentState++;
                        timeToggle = true;
                    }
                }else if (currentState == 6){
                    drive.setMaxPower(.5);
                    drive.lineTo(25,25,Math.toRadians(180));
                    if(drive.isAtTarget()){
                        currentState++;
                    }
                }else if(currentState == 7){
                    intake.setPower(.35);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 2000){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 8){
                    intake.setPower(0);
                    slides.setState(DepoSlides.DepositState.DOWN);
                    arm1.setState(DepoArm.DepoArmState.INTERMEDIATE);
                    wrist1.setState(Wrist.WristState.ABOVE_TRANSFER);
                    drive.setMaxPower(.5);
                    drive.lineTo(45,37,Math.toRadians(180));
                    if(drive.isAtTarget()){
                        currentState++;
                    }
                }else if(currentState == 9){
                    drive.lineTo(50, 60,Math.toRadians(180));
                    intake.setState(LTIntake.IntakeState.INTAKE_TELE);
                    if(drive.isAtTarget()){
                        currentState++;
                    }
                }else if(currentState == 10){ //retraction sequence
                    slides.setState(DepoSlides.DepositState.DOWN);
                    pitch.setState(Pitch.PitchState.INITIALIZE);
                    arm1.setState(DepoArm.DepoArmState.INTERMEDIATE);
                    wrist1.setState(Wrist.WristState.ABOVE_TRANSFER);
                    if(Math.abs(pitch.getCurrentPosition() - pitch.target) < .05){
                        if(timeToggle){//timeToggle starts at true by default
                            TimeStamp = timer.milliseconds();
                            timeToggle = false;
                        }
                        if(timer.milliseconds() > TimeStamp + 500){
                            currentState++;
                            timeToggle = true;
                        }
                    }
                }else if(currentState == 10){
                    arm1.setState(DepoArm.DepoArmState.ABOVE_TRANSFER);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 500){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 11){
                    arm1.setState(DepoArm.DepoArmState.TRANSFER);
                    wrist1.setState(Wrist.WristState.TRANSFER); //when new guide gets added
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 500){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 12){
                    wrist1.setState(Wrist.WristState.TRANSFER);
                    slides.setState(DepoSlides.DepositState.OVER_IN);
                }



            }else if(randomization == 0){ //left (closest to center of feild)
                if(currentState == 0){
                    drive.setMaxPower(.7);
                    drive.lineTo(50, 33, Math.toRadians(180));
                    if(drive.isAtTarget()){
                        currentState++;
                    }
                }else if(currentState == 1){
                    pitch.setState(Pitch.PitchState.AUTON_PRELOAD);
                    if(Math.abs(pitch.getCurrentPosition() - pitch.target) < .05){
                        currentState++;
                    }
                }else if(currentState == 2){
                    arm1.setState(DepoArm.DepoArmState.LT_SCORE);
                    wrist1.setState(Wrist.WristState.LT_SCORE);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 500){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 3){
                    slides.setState(DepoSlides.DepositState.AUTO_PRELOAD_SCORE);
//                    intake.setState(LTIntake.IntakeState.INTAKE_TELE);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 1000){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 4){
                    claw.setState(Claw.ClawState.UNLATCHED);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 750){
                        currentState++;
                        timeToggle = true;
                    }
                }else if (currentState == 5){
                    drive.setMaxPower(.2);
                    drive.lineTo(47, 29, Math.toRadians(180));
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 1500){
                        currentState++;
                        timeToggle = true;
                    }
                }else if (currentState == 6){
                    drive.setMaxPower(.5);
                    drive.lineTo(14.5,37,Math.toRadians(180));
                    if(drive.isAtTarget()){
                        currentState++;
                    }
                }else if(currentState == 7){
                    intake.setPower(.7);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 2000){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 8){
                    intake.setPower(-.1);
                    slides.setState(DepoSlides.DepositState.DOWN);
                    arm1.setState(DepoArm.DepoArmState.INTERMEDIATE);
                    wrist1.setState(Wrist.WristState.ABOVE_TRANSFER);
                    drive.setMaxPower(.5);
                    drive.lineTo(45,37,Math.toRadians(180));
                    if(drive.isAtTarget()){
                        currentState++;
                    }
                }else if(currentState == 9){
                    drive.lineTo(50, 60,Math.toRadians(180));
                    intake.setState(LTIntake.IntakeState.INTAKE_TELE);
                    if(drive.isAtTarget()){
                        currentState++;
                    }
                }else if(currentState == 10){ //retraction sequence
                    slides.setState(DepoSlides.DepositState.DOWN);
                    pitch.setState(Pitch.PitchState.INITIALIZE);
                    arm1.setState(DepoArm.DepoArmState.INTERMEDIATE);
                    wrist1.setState(Wrist.WristState.ABOVE_TRANSFER);
                    if(Math.abs(pitch.getCurrentPosition() - pitch.target) < .05){
                        if(timeToggle){//timeToggle starts at true by default
                            TimeStamp = timer.milliseconds();
                            timeToggle = false;
                        }
                        if(timer.milliseconds() > TimeStamp + 500){
                            currentState++;
                            timeToggle = true;
                        }
                    }
                }else if(currentState == 10){
                    arm1.setState(DepoArm.DepoArmState.ABOVE_TRANSFER);
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 500){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 11){
                    arm1.setState(DepoArm.DepoArmState.TRANSFER);
                    wrist1.setState(Wrist.WristState.TRANSFER); //when new guide gets added
                    if(timeToggle){//timeToggle starts at true by default
                        TimeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if(timer.milliseconds() > TimeStamp + 500){
                        currentState++;
                        timeToggle = true;
                    }
                }else if(currentState == 12){
                    wrist1.setState(Wrist.WristState.TRANSFER);
                    slides.setState(DepoSlides.DepositState.OVER_IN);
                }
            }





            drive.update();
            pitch.update();
            slides.update();
            wrist1.update();
            arm1.update();
            intake.update();
            claw.update();
        }
    }
}
