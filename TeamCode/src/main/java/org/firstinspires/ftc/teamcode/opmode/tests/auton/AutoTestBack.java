package org.firstinspires.ftc.teamcode.opmode.tests.auton;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
import com.acmerobotics.roadrunner.geometry.Pose2d;
import com.acmerobotics.roadrunner.geometry.Vector2d;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;

import org.firstinspires.ftc.teamcode.drive.posePID2.DT;

@TeleOp(name = "auto test back")
@Config
public class AutoTestBack extends LinearOpMode {

    enum State {
        PROP,
        STACK,
        DEPOSIT
    }
    //start locs
    Pose2d startBPose = new Pose2d(-35, -65, Math.toRadians(-90)); //default position
    Pose2d startFPose = new Pose2d(11, -65, Math.toRadians(-90));
    //props
    Pose2d leftBProp = new Pose2d(-35, -32, Math.toRadians(-180));
    Vector2d leftBPropIntermediate = new Vector2d(-37, -32);
    Vector2d leftFProp = new Vector2d(10, -31);
    Vector2d leftFPropIntermediate = new Vector2d(14,-31);

    Vector2d rightBProp = new Vector2d(-35, -32); //rn
    Vector2d rightBPropIntermediate = new Vector2d(-32, -32); //rn
    Vector2d rightFProp = new Vector2d(31, -32);
    Vector2d rightFPropIntermediate = new Vector2d(38, -32);

    Vector2d middleBProp = new Vector2d(-35, -12);
    Vector2d middleBPropIntermediate = new Vector2d(-35,-14);
    Vector2d middleFProp = new Vector2d(20, -24);


    //stack
    Vector2d beforeStack = new Vector2d(-37, -12);
    Vector2d stackPos = new Vector2d(-54, -12);

    Vector2d FStackI2 = new Vector2d(35, -60);
    Vector2d FStackI1 = new Vector2d(-37, -60);
    Vector2d FStack = new Vector2d(-54, -36);
    //deposit
    Vector2d dropOff = new Vector2d(32, -12);

    Pose2d depositL = new Pose2d(35, -16, Math.toRadians(-200));
    Pose2d afterDepo = new Pose2d(35, -12, Math.toRadians(-180));

    Pose2d FFirstR = new Pose2d(45, -42, Math.toRadians(-90));
    Vector2d FFirstM = new Vector2d(45, -35);
    Pose2d FFirstL = new Pose2d(45, -28, Math.toRadians(-90));
    Vector2d runToBoardPos = new Vector2d(55, -12);

    State currentState;

    int randomization = 0;
    int intermediaterandomizationstate =0;
    @Override
    public void runOpMode() throws InterruptedException {
        DT drive = new DT(hardwareMap, new Pose2d(startBPose.getX(), startBPose.getY(), startBPose.getHeading()));
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        waitForStart();
        while(opModeIsActive()){
            switch(currentState){
                case PROP:
                    if(randomization==0){
                        if(intermediaterandomizationstate == 0){
                            drive.lineTo(leftBProp.getX(), leftBProp.getY(), leftBProp.getHeading());

                            if(drive.isAtTarget()){
                                intermediaterandomizationstate++;
                            }
                        }else if(intermediaterandomizationstate == 1){
                            drive.lineToCHeading(leftBPropIntermediate.getX(), leftBPropIntermediate.getY());

                            if(drive.isAtTarget()){
                                intermediaterandomizationstate++;
                            }
                        }else if(intermediaterandomizationstate == 2){
                            drive.lineToCHeading(leftBProp.getX(), leftBProp.getY());

                            if(drive.isAtTarget()){
                                intermediaterandomizationstate++;
                            }
                        }else if(intermediaterandomizationstate == 3){
                            drive.lineToCHeading(beforeStack.getX(), beforeStack.getY());

                            if(drive.isAtTarget()){
                                intermediaterandomizationstate = 0;
                                currentState = State.STACK;
                            }
                        }
                    }else if(randomization==1){

                    }else{

                    }
                    break;
                case STACK:

                    if(randomization==0){
                        if(intermediaterandomizationstate == 0){
                            drive.lineToCHeading(stackPos.getX(), stackPos.getY());

                            if(drive.isAtTarget()){
                                currentState = State.DEPOSIT;
                            }
                        }
                    }else if(randomization==1){

                    }else{

                    }
                    break;
                case DEPOSIT:
                    if(randomization==0){
                        if(intermediaterandomizationstate == 0){
                            drive.lineToCHeading(dropOff.getX(), dropOff.getY());

                            if(drive.isAtTarget()){
                                intermediaterandomizationstate++;
                            }
                        }else if(intermediaterandomizationstate == 1){
                            drive.lineTo(depositL.getX(), depositL.getY(), depositL.getHeading());

                            if(drive.isAtTarget()){
                                intermediaterandomizationstate++;
                            }
                        }else if(intermediaterandomizationstate == 2){
                            drive.lineTo(afterDepo.getX(), afterDepo.getY(), afterDepo.getHeading());

                            if(drive.isAtTarget()){
                                currentState = State.STACK;
                                intermediaterandomizationstate =0;
                            }
                        }
                    }else if(randomization==1){

                    }else{

                    }
                    break;

            }
            drive.update();
            telemetry.update();
        }
    }
}
