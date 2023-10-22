package org.firstinspires.ftc.teamcode.hardware;


import org.firstinspires.ftc.teamcode.drive.MecanumDrive;
import org.firstinspires.ftc.teamcode.hardware.Deposit.DepoSlides;
import org.firstinspires.ftc.teamcode.hardware.Deposit.DepoTurret;
import org.firstinspires.ftc.teamcode.hardware.Deposit.Pitch;
import org.firstinspires.ftc.teamcode.hardware.Intake.Intake;
import org.firstinspires.ftc.teamcode.hardware.Sensors.Blinkdin;
import org.firstinspires.ftc.teamcode.hardware.Sensors.Imu;
import org.firstinspires.ftc.teamcode.hardware.Sensors.axonEncoder;

public class Robot {
    public MecanumDrive drive;
    public DepoSlides depoSlides;
    public Pitch pitch;
    public DepoTurret depoTurret;
    public Intake intake;
    public Imu imu;
    public axonEncoder turretEncoder;
    public Blinkdin LED;
    public Robot(MecanumDrive drive, DepoSlides depoSlides, Pitch pitch, DepoTurret depoTurret, Intake intake, Imu imu, axonEncoder turretEncoder, Blinkdin LED) {
        this.drive = drive;
        this.depoSlides = depoSlides;
        this.pitch = pitch;
        this.depoTurret = depoTurret;
        this.intake = intake;
        this.turretEncoder = turretEncoder;
        this.LED = LED;

        this.imu = imu;
        this.imu.init();
    }
}
