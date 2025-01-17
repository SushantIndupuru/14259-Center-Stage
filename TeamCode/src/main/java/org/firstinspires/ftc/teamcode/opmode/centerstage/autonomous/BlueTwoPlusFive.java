package org.firstinspires.ftc.teamcode.opmode.centerstage.autonomous;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.control.PIDCoefficients;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.trajectory.Trajectory;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.hardware.DcMotorEx;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.util.ElapsedTime;

import org.firstinspires.ftc.teamcode.drive.SampleMecanumDrive;
import org.firstinspires.ftc.teamcode.hardware.Deposit.DepoArm;
import org.firstinspires.ftc.teamcode.hardware.Deposit.DepoSlides;
import org.firstinspires.ftc.teamcode.hardware.Deposit.LM1Turret;
import org.firstinspires.ftc.teamcode.hardware.Intake.Intake;
import org.firstinspires.ftc.teamcode.usefuls.Motor.DcMotorBetter;
import org.firstinspires.ftc.teamcode.usefuls.Motor.ServoMotorBetter;

@Autonomous(name = "2+5 Blue")
@Config
@Disabled
public class BlueTwoPlusFive extends OpMode {

    enum State {
        IDLE,
        TOINTAKE,
        TOGPL,
        TOSCOREL,
        INTAKE,
        PARK,
        SCORE,
        RESETTOSTACK,
        STACK, //picks up pixels from stack
        BOARD, //to scoring board
    }

    public static int randomization = 0;
    //0 means left, 1 means middle, 2 means right.
    SampleMecanumDrive drive;
    boolean timeToggle = true;
    double timeStamp = 0;
    Intake intake;

    int cycles_left = 3;

    ElapsedTime timer = new ElapsedTime();
    LM1Turret turret;
    DepoArm arm;
    DepoSlides slides;

    State currentstate;


    //First Diverge

    Pose2d startPose = new Pose2d(-39, 61, Math.toRadians(90));
    Pose2d leftProp = new Pose2d(-46, 29, Math.toRadians(90));
    Pose2d leftPropIP = new Pose2d(-46, 15, Math.toRadians(90));
    Pose2d middleProp = new Pose2d(-34, 12, Math.toRadians(90));
    Pose2d rightProp = new Pose2d(-35, 29, Math.toRadians(0));
    Pose2d rightPropStack = new Pose2d(-58, 11, Math.toRadians(-180));
    Pose2d toStack = new Pose2d(-59, 12, Math.toRadians(180));

    Pose2d middleTruss = new Pose2d(0, 7, Math.toRadians(180));
    Pose2d depositL = new Pose2d(38, 8, Math.toRadians(220));

    Pose2d park = new Pose2d(50,30,Math.toRadians(180));


    boolean fullyReset = true;
    Trajectory toPark;
    Trajectory toLeftStack;
    Trajectory LeftBackFiveInches;
    Trajectory toLeftProp;
    Trajectory pickUpStack;
    Trajectory ScoreLeft;
    Trajectory leftToTruss;
    Trajectory toRightStack;
    Trajectory leftToIntake;
    Trajectory toTruss;
    Trajectory [] toProps;
    Trajectory [] toStacks;
    Trajectory toMiddleStack;
    // Test Commit

    @Override
    public void init() {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());

        drive = new SampleMecanumDrive(hardwareMap);
        intake = new Intake(hardwareMap.get(DcMotorEx.class, "Intake"), new ServoMotorBetter(hardwareMap.get(Servo.class, "intakeArm")));
        intake.setState(Intake.IntakeState.INITIALIZE);
        intake.update();
        turret = new LM1Turret(new ServoMotorBetter(hardwareMap.get(Servo.class, "turret")));
        arm = new DepoArm(new ServoMotorBetter(hardwareMap.get(Servo.class, "arm")), new ServoMotorBetter(hardwareMap.get(Servo.class, "fake")));
        slides = new DepoSlides(new DcMotorBetter(hardwareMap.get(DcMotorEx.class, "leftSlides")), new DcMotorBetter(hardwareMap.get(DcMotorEx.class, "rightSlides")));

        slides.passive = false;
        slides.pidRunning = true;
        slides.manualMode = false;

        drive.setPoseEstimate(startPose);

        toProps = new Trajectory[]{
                drive.trajectoryBuilder(startPose)
                        .lineToLinearHeading(leftProp)
                        .build(),
                drive.trajectoryBuilder(startPose)
                        .lineToLinearHeading(middleProp)
                        .build(),
                drive.trajectoryBuilder(startPose)
                        .lineToLinearHeading(rightProp)
                        .build()

        };

        LeftBackFiveInches = drive.trajectoryBuilder(toProps[randomization].end())
                .lineToLinearHeading(leftPropIP)
                .build();
        toStacks = new Trajectory[]{
                drive.trajectoryBuilder(LeftBackFiveInches.end())
                        .lineToSplineHeading(toStack)
                        .build(),
                drive.trajectoryBuilder(toProps[randomization].end())
                        .lineToLinearHeading(toStack)
                        .build(),
                drive.trajectoryBuilder(toProps[randomization].end())
                        .lineToLinearHeading(rightPropStack)
                        .build()


        };



//        toTruss = drive.trajectoryBuilder(toStacks[randomization].end())
//                .lineToLinearHeading(middleTruss)
//                .addDisplacementMarker(() -> drive.followTrajectoryAsync(ScoreLeft))
//                .build();
        ScoreLeft = drive.trajectoryBuilder(toStacks[randomization].end())
                .lineToSplineHeading(depositL)
                .build();
        leftToIntake = drive.trajectoryBuilder(ScoreLeft.end())
                .lineToLinearHeading(toStack)
                .build();
        toPark = drive.trajectoryBuilder(ScoreLeft.end())
                .lineToLinearHeading(park)
                .build();

    }
    public void init_loop(){
        telemetry.addData("random", randomization);
        currentstate = State.TOGPL;
        drive.followTrajectoryAsync(toProps[randomization]);
        telemetry.update();
    }

    @Override

    public void loop() {
        switch (currentstate) {
            case TOGPL:
                if (!drive.isBusy()) {
                    arm.manualPosition = 0.04;
                    intake.manualPosition = 0.3;
                    currentstate = State.TOINTAKE;
                    if(randomization==0){
                        drive.followTrajectoryAsync(LeftBackFiveInches);
                    }
                }
                break;
            case TOINTAKE:
                if (!drive.isBusy()) {
                    currentstate = State.INTAKE;
                    drive.followTrajectoryAsync(toStacks[randomization]);
                    intake.setPower(-0.8);
                }
                break;
            case INTAKE:
                if(!drive.isBusy() && atTargetPosition(toStack)) {
                    if (timeToggle) {
                        timeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if (timer.milliseconds() > timeStamp + 100) {
                        drive.followTrajectoryAsync(ScoreLeft);
                        SampleMecanumDrive.HEADING_PID = new PIDCoefficients(0, 0, 0);
                        currentstate = State.TOSCOREL;
                        timeToggle = true;
                    }

                }
                break;
            case TOSCOREL:
                if(drive.getPoseEstimate().getX() > -45)
                    intake.setPower(1);
                if(drive.getPoseEstimate().getX() > 5){
                    arm.manualPosition = 0.6;
                    slides.manualPosition = 22;
                    fullyReset = false;
                    if (timeToggle) {
                        timeStamp = timer.milliseconds();
                        timeToggle = false;
                    }
                    if (timer.milliseconds() > timeStamp + 400) {
                        turret.manualPosition = 0.5;
                        timeToggle = true;
                    }
                    if(!drive.isBusy()){
                        currentstate = State.SCORE;
                    }
                }
                if(drive.getPoseEstimate().getX() > 12){
                    SampleMecanumDrive.HEADING_PID = new PIDCoefficients(8, 0, 0);
                    //got any other bad ideas, Leo?
                    //literally a tragedy
                }
                break;

            case SCORE:
                if(atTargetPosition(depositL)){
                    arm.manualPosition = 0.3;
                }
                if (timeToggle) {
                    timeStamp = timer.milliseconds();
                    timeToggle = false;
                }
                if (timer.milliseconds() > timeStamp + 1000) {
                    arm.manualPosition = 0.5;
                    currentstate = State.RESETTOSTACK;
                    intake.setPower(-0.8);
                    cycles_left--;
                    timeToggle = true;
                }
                if(cycles_left == 0) {
                    currentstate = State.PARK;
                }
                break;

            case RESETTOSTACK:
                if(!fullyReset){
                    arm.manualPosition = 0.5;
                    turret.manualPosition = 0.01;
                    slides.manualPosition = 0;
//                    intake.setPower(-.6);
                    if(slides.getCurrentPosition() > -18){
                        drive.followTrajectoryAsync(leftToIntake);
                    }
                    if(slides.getCurrentInches() > -3){
                        arm.manualPosition = 0.04;
                        fullyReset = true;
                    }
                }else{
                    intake.manualPosition -= 0.155;
                    currentstate = State.INTAKE;
                }
                break;
            case PARK:
                if(!fullyReset) {
                    arm.manualPosition = 0.5;
                    turret.manualPosition = 0.01;
                    slides.manualPosition = 0;
//                    intake.setPower(-.6);
                    if (slides.getCurrentPosition() > -18) {
                        drive.followTrajectoryAsync(toPark);
                    }
                    if (slides.getCurrentInches() > -3) {
                        arm.manualPosition = 0.04;
                        fullyReset = true;
                    }
                }else{
                    telemetry.addData("DID YOU DO SCORE A 2+5?", false);
                    telemetry.addLine("ASLKJFD HASLKJD AHSLKDJ ASLDJK ASLKJD BASLKDJ ASD ");
                    SampleMecanumDrive.HEADING_PID = new PIDCoefficients(8, 0, 0);
                }

        }

        intake.setState(Intake.IntakeState.RUNTOPOSITION);
        slides.setState(DepoSlides.DepositState.RUNTOPOSITION);
        turret.setState(LM1Turret.TurretState.RUNTOPOSITION);
        arm.setState(DepoArm.DepoArmState.RUNTOPOSITION);
        slides.update();
        arm.update();
        turret.update();
        intake.update();
        drive.update();
        telemetry.addData("Robot Angle", drive.getPoseEstimate().getHeading());
        telemetry.addData("state: ", currentstate);
        telemetry.addData("slides position", slides.getCurrentInches());
        telemetry.addData("Arm Manual Position = ", arm.manualPosition);
        telemetry.addData("intake Position:" , intake.manualPosition);


    }

    public boolean atTargetPosition(Pose2d targetPose){
        double deltaX = targetPose.getX() - drive.getPoseEstimate().getX();
        double deltaY = targetPose.getY() - drive.getPoseEstimate().getY();
        double deltaR = targetPose.getHeading() - drive.getPoseEstimate().getHeading();
        telemetry.addData("delta Sum", Math.abs(deltaX) + Math.abs(deltaY) + Math.abs(deltaR));
        return deltaX + deltaY + deltaR < 10;
    }
}