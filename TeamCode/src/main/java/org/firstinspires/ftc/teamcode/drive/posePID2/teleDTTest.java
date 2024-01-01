package org.firstinspires.ftc.teamcode.drive.posePID2;

import com.qualcomm.robotcore.eventloop.opmode.Disabled;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.eventloop.opmode.TeleOp;
import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.telemetry.MultipleTelemetry;
@TeleOp
@Disabled
public class teleDTTest extends LinearOpMode {
    @Override
    public void runOpMode() throws InterruptedException {
        telemetry = new MultipleTelemetry(telemetry, FtcDashboard.getInstance().getTelemetry());
        DT drive = new DT(hardwareMap);
        waitForStart();
        while(opModeIsActive()){
            drive.setPowers(gamepad1.left_stick_x, gamepad1.left_stick_y, gamepad1.right_stick_x);
            drive.update();
            telemetry.addData("x pos", drive.getX());
            telemetry.addData("x power", drive.getPowerX());
            telemetry.update();
        }
    }
}