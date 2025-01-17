package org.firstinspires.ftc.teamcode.hardware.Intake;
import com.qualcomm.robotcore.hardware.DcMotorEx;

import org.firstinspires.ftc.teamcode.hardware.Deposit.DepoArm;
import org.firstinspires.ftc.teamcode.hardware.Deposit.LM1Turret;
import org.firstinspires.ftc.teamcode.usefuls.Motor.ServoMotorBetter;

public class LTIntake{
    public enum IntakeState {
        INITIALIZE,
        INTAKE_TELE,
        STACK_HIGH,
        UP,
        RUNTOPOSITION,
        INCRIMENT_UP,
        INCRIMENT_DOWN,
        AUTO_HIGH,
        AUTO_STACK_DROPPED,
        LEVEL2,
        STOPPED
    }

    public IntakeState intakeFSM = IntakeState.INTAKE_TELE;
    private static final double LOWER_BOUND = 0;
    private static final double UPPER_BOUND = 1;
    private static final double OtherLowerBound = 1;
    private static final double OtherUpperBound = 0;
    public double intakePower = 0;
    public DcMotorEx intakeMotor;
    //NOT A DcMotorBetter, just a DcMotorEx

    public ServoMotorBetter intakePivot;
    public ServoMotorBetter intakePivot2;


    public double target = 0;
    public double target2 = 0;
    public double manualPosition = 0;
    public LTIntake(DcMotorEx intakeMotor, ServoMotorBetter intakePivot, ServoMotorBetter intakePivot2) {
        this.intakeMotor = intakeMotor;
        this.intakePivot = intakePivot;
        this.intakePivot.setLowerBound(LOWER_BOUND);
        this.intakePivot.setUpperBound(UPPER_BOUND);
        this.intakePivot2 = intakePivot2;
        this.intakePivot2.setLowerBound(OtherLowerBound);
        this.intakePivot2.setLowerBound(OtherUpperBound);
    }
    public LTIntake stop(){
        intakePower = 0;
        return this;
    }
    public LTIntake setPower(double p){
        intakePower = p;
        return this;
    }
    public void setState(IntakeState state){
        this.intakeFSM = state;
        switch (intakeFSM){
            case RUNTOPOSITION:
                target = manualPosition;
                break;
            case INITIALIZE:
                target2 = .2;
                target = .8;
                break;
            case INTAKE_TELE:
                target2= .6;
                target = .4;
                break;
            case STACK_HIGH:
                target2 = .56;
                target = .44;
                break;
            case UP:
                target = 0.1;
                break;
            case INCRIMENT_UP:
                target-=.05;
                break;
            case INCRIMENT_DOWN:
                target+=.05;
                break;
            case AUTO_HIGH:
                target = .3;
                break;
            case LEVEL2:
                target = .15;
                break;
            case AUTO_STACK_DROPPED:
                target = .23;
            case STOPPED:
                break;
        }
    }
    public boolean isBusy() { return this.intakeMotor.isBusy();}
    public IntakeState getState(){
        return intakeFSM;
    }

    public double getPosition(){
        return target;
    }
    public void update() {
        this.intakeMotor.setPower(intakePower);
        this.intakePivot.setPosition(target);
        this.intakePivot2.setPosition(target2);
        this.intakePivot2.update();
        this.intakePivot.update();
    }

}
