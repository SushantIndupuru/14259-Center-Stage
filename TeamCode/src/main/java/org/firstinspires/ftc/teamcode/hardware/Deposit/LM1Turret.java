package org.firstinspires.ftc.teamcode.hardware.Deposit;


import com.acmerobotics.dashboard.config.Config;
import org.firstinspires.ftc.teamcode.usefuls.Motor.ServoMotorBetter;

@Config
public class LM1Turret {
    public enum TurretState {
        INITIALIZE,
        SCORE,
        PITCH_MID,
        CALCAULATE,
        STOPPED
    }

    public TurretState turretFSM = TurretState.STOPPED;

    private static final double LEFT_LOWER_BOUND = 1;
    private static final double LEFT_UPPER_BOUND = .14;

    private ServoMotorBetter turret;

    public double target = 0;

    public LM1Turret(ServoMotorBetter leftArm) {
        this.turret = leftArm;
        this.turret.setLowerBound(LM1Turret.LEFT_LOWER_BOUND);
        this.turret.setUpperBound(LM1Turret.LEFT_UPPER_BOUND);
    }
    public LM1Turret.TurretState getState(){
        return turretFSM;
    }

    public void setState(TurretState state){
        this.turretFSM = state;
        switch (turretFSM){
            case INITIALIZE:
                target = 0.01;
                break;
            case SCORE:
                target = 0.65;
            case CALCAULATE:
                break;
            case STOPPED:
                break;
        }
    }

    public void update(){
        this.turret.setPosition(target);
        this.turret.update();
    }
}
