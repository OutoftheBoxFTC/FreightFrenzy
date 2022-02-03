package Opmodes.Auto;

import com.acmerobotics.dashboard.FtcDashboard;
import com.acmerobotics.dashboard.config.Config;
import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.util.RobotLog;

import Drive.DriveSystem;
import Hardware.HardwareSystems.FFSystems.Actions.BlueGoalActions;
import Hardware.HardwareSystems.FFSystems.Actions.EnterIntakeAction;
import Hardware.HardwareSystems.FFSystems.Actions.LeaveIntakeAction;
import Hardware.HardwareSystems.FFSystems.Actions.MoveExtensionAction;
import Hardware.HardwareSystems.FFSystems.Actions.MovePitchAction;
import MathSystems.Angle;
import MathSystems.MathUtils;
import MathSystems.Position;
import MathSystems.Vector.Vector3;
import Odometry.FusionOdometer;
import Opmodes.BasicOpmode;
import State.Action.Action;
import State.Action.ActionController;
import State.Action.ActionQueue;
import State.Action.InstantAction;
import State.Action.StandardActions.DelayAction;
import Utils.OpmodeData;
import Utils.OpmodeStatus;
import Utils.PathUtils.ContinousPathBuilder;
import Utils.PathUtils.Path;
import Utils.PathUtils.PathBuilder;
import Vision.ApriltagDetector;

@Autonomous
@Config
public class BlueAuto extends BasicOpmode {
    public static LEVEL level = LEVEL.MED;

    Position position, velocity;

    //double[] offsets = new double[]{0, 20, 10};
    double[] offsets = new double[]{15, 15, 15, 15};
    long started = 0;

    @Override
    public void setup() {
        ApriltagDetector detector = new ApriltagDetector(hardwareMap);
        ActionController.addAction(detector);
        position = Position.ZERO();
        velocity = Position.ZERO();

        ActionController.addAction(() -> OpmodeData.getInstance().setExtensionPos(hardware.getTurretSystem().getExtensionPosition()));

        ActionController.addAction(() -> {
            telemetry.addData("Position", position);
            telemetry.addData("Detection", detector.getPosition());
            FtcDashboard.getInstance().getTelemetry().addData("Left", hardware.getOdometrySystem().getLeftDist());
            FtcDashboard.getInstance().getTelemetry().addData("Forward", hardware.getOdometrySystem().getRightDist());
            FtcDashboard.getInstance().getTelemetry().update();
            if(!isStarted()) {
                switch (detector.getPosition()) {
                    case LEFT:
                        level = LEVEL.LOW;
                        break;
                    case CENTRE:
                        level = LEVEL.MED;
                        break;
                    case RIGHT:
                        level = LEVEL.HIGH;
                        break;
                }
            }
        });

        FusionOdometer odometer = new FusionOdometer(hardware.getOdometrySystem(), hardware.getDrivetrainSystem(), position, velocity);
        OpmodeStatus.bindOnStart(odometer);

        OpmodeStatus.bindOnStart(new InstantAction() {
            @Override
            public void update() {
                started = System.currentTimeMillis();
                detector.shutdown();
            }
        });

        DriveSystem system = new DriveSystem(hardware.getDrivetrainSystem(), position);

        hardware.getDrivetrainSystem().setZeroPowerBehaviour(DcMotor.ZeroPowerBehavior.BRAKE);

        ActionQueue initQueue = new ActionQueue();
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().closeArm();
            }
        });
        initQueue.submitAction(new LeaveIntakeAction(hardware));
        initQueue.submitAction(new DelayAction(1000));
        initQueue.submitAction(new Action() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(250);
                hardware.getTurretSystem().closeArm();
            }

            @Override
            public boolean shouldDeactivate() {
                return hardware.getTurretSystem().isExtensionAtPos();
            }
        });
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().setBucketPosRaw(1);
            }
        });
        initQueue.submitAction(new DelayAction(500));
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getTurretSystem().moveExtensionRaw(0);
            }
        });
        initQueue.submitAction(new DelayAction(2000));
        initQueue.submitAction(new InstantAction() {
            @Override
            public void update() {
                hardware.getDrivetrainSystem().setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-40));
                hardware.getTurretSystem().movePitchRaw(Angle.degrees(1));
            }
        });

        ActionController.addAction(initQueue);

        OpmodeStatus.bindOnStart(new InstantAction() {
            @Override
            public void update() {
                ActionQueue runQueue = new ActionQueue();
                if(level == LEVEL.LOW) {
                    runQueue.submitAction(system.gotoGvf(new Position(8, 0, Angle.ZERO())));
                }else{
                    runQueue.submitAction(system.gotoGvf(new Position(12, 0, Angle.ZERO())));
                }
                runQueue.submitAction(new Action() {
                    @Override
                    public void update() {
                        hardware.getTurretSystem().setBucketPosRaw(1);
                        if(level == LEVEL.HIGH) {
                            MoveExtensionAction.P = -0.005;
                            hardware.getTurretSystem().movePitchRaw(Angle.degrees(-20));
                            hardware.getTurretSystem().moveExtensionRaw(465);
                            hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-50));
                        }
                        if(level == LEVEL.MED){
                            MoveExtensionAction.P = -0.002;
                            hardware.getTurretSystem().movePitchRaw(Angle.degrees(9));
                            hardware.getTurretSystem().moveExtensionRaw(464);
                            hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-50));
                            hardware.getTurretSystem().setBucketPosRaw(0.85);
                        }
                        if(level == LEVEL.LOW){
                            MoveExtensionAction.P = -0.001;
                            hardware.getTurretSystem().movePitchRaw(Angle.degrees(17));
                            hardware.getTurretSystem().moveExtensionRaw(453);
                            hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-50));
                            hardware.getTurretSystem().setBucketPosRaw(0.85);
                        }
                    }

                    @Override
                    public boolean shouldDeactivate() {
                        return hardware.getTurretSystem().isExtensionAtPos();
                    }
                });
                runQueue.submitAction(new Action() {
                    @Override
                    public void update() {
                    }

                    @Override
                    public boolean shouldDeactivate() {
                        return hardware.getTurretSystem().isExtensionAtPos();
                    }
                });
                runQueue.submitAction(new DelayAction(250));
                runQueue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        MoveExtensionAction.P = -0.001;
                        hardware.getTurretSystem().openArm();
                    }
                });

                runQueue.submitAction(new DelayAction(500));

                runQueue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        hardware.getTurretSystem().closeArm();
                        hardware.getTurretSystem().setBucketPosRaw(0.45);
                    }
                });
                runQueue.submitAction(new DelayAction(250));
                runQueue.submitAction(BlueGoalActions.getBlueAllianceReturnAuto(hardware));
                runQueue.submitAction(new Action() {
                    @Override
                    public void update() {
                        hardware.getTurretSystem().moveTurretRaw(Angle.degrees(0));
                        hardware.getTurretSystem().openArm();
                    }

                    @Override
                    public boolean shouldDeactivate() {
                        return true;
                    }
                });

                Path strafeLeft = new PathBuilder(new Position(10, -2, Angle.degrees(0)))
                        .lineTo(new Position(3, -2, Angle.ZERO())).build();

                runQueue.submitAction(system.followGvf(strafeLeft));

                double offset = 0;

                for(int i = 0; i < 3; i ++) {
                    Path driveInFirst = new ContinousPathBuilder(strafeLeft.getEndpoint())
                            .lineTo(new Position(3, 38 - offset, Angle.ZERO()))
                            .build();

                    Path intake1 = new ContinousPathBuilder(driveInFirst.getEndpoint())
                            .lineTo(new Position(5, (54 + (i * 4)) - offset, Angle.ZERO()))
                            .build();

                    runQueue.submitAction(new DelayAction(300));
                    /**
                    runQueue.submitAction(new Action() {
                        @Override
                        public void update() {
                            hardware.getDrivetrainSystem().setPower(new Vector3(0, 0.8, 0));
                        }

                        @Override
                        public boolean shouldDeactivate() {
                            return position.getY() > 30;
                        }
                    });
                    */
                    runQueue.submitAction(system.followGvf(driveInFirst));

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            hardware.getDrivetrainSystem().setPower(Vector3.ZERO());
                        }
                    });

                    offset = offsets[i + 1];

                    runQueue.submitAction(new EnterIntakeAction(hardware));

                    runQueue.submitAction(new Action() {
                        @Override
                        public void update() {

                        }

                        @Override
                        public boolean shouldDeactivate() {
                            return hardware.getTurretSystem().getExtensionPosition() < 10;
                        }
                    });

                    runQueue.submitAction(new DelayAction(500));

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            long now = System.currentTimeMillis();
                            double remaining = 30 - MathUtils.millisToSec(now - started);
                            if(remaining < 6){
                                ActionController.getInstance().terminateAction(runQueue);
                            }
                        }
                    });

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            hardware.getTurretSystem().setBucketPosRaw(0.1);
                            hardware.getIntakeSystem().setPower(1);
                        }
                    });

                    runQueue.submitAction(new Action() {
                        Action action = system.followGvf(intake1);
                        long timer = 0;
                        boolean deactivate = false;

                        @Override
                        public void initialize() {
                            ActionController.addAction(action);
                            timer = System.currentTimeMillis() + 1000;
                        }

                        @Override
                        public void update() {
                            deactivate = System.currentTimeMillis() > timer;
                            if(deactivate)
                                ActionController.getInstance().terminateAction(action);
                        }

                        @Override
                        public boolean shouldDeactivate() {
                            return deactivate;
                        }
                    });

                    runQueue.submitAction(new DelayAction(100));

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            hardware.getIntakeSystem().setPower(-1);
                            hardware.getDrivetrainSystem().setPower(new Vector3(0, -0.5, 0));
                        }
                    });

                    runQueue.submitAction(new DelayAction(250));

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            hardware.getDrivetrainSystem().setPower(new Vector3(0, 0, 0));
                            hardware.getIntakeSystem().setPower(1);
                        }
                    });

                    runQueue.submitAction(new LeaveIntakeAction(hardware));

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            ActionQueue queue = new ActionQueue();
                            queue.submitAction(new DelayAction(400));
                            queue.submitAction(new Action() {
                                @Override
                                public void update() {

                                }

                                @Override
                                public boolean shouldDeactivate() {
                                    return hardware.getTurretSystem().isExtensionAtPos();
                                }
                            });
                            queue.submitAction(new InstantAction() {
                                @Override
                                public void update() {
                                    hardware.getTurretSystem().moveExtensionRaw(250);
                                    hardware.getTurretSystem().setBucketPosRaw(1);
                                    hardware.getTurretSystem().moveTurretRaw(Angle.degrees(-49));
                                }
                            });

                            ActionController.addAction(queue);
                        }
                    });

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            odometer.setUse2mForward(true);
                        }
                    });

                    runQueue.submitAction(new DelayAction(250));

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            odometer.setUse2mForward(false);
                            hardware.getIntakeSystem().setPower(0);
                        }
                    });

                    Path outtake1 = new ContinousPathBuilder(intake1.getEndpoint())
                            .lineTo(new Position(5, 0 - offset, Angle.ZERO()))
                            .build();

                    runQueue.submitAction(system.followGvf(outtake1));

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            hardware.getDrivetrainSystem().setPower(Vector3.ZERO());
                        }
                    });

                    runQueue.submitAction(new DelayAction(100));

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            //odometer.setUse2mForward(false);
                            hardware.getIntakeSystem().setPower(0);
                        }
                    });

                    runQueue.submitAction(BlueGoalActions.getBlueAlliance(hardware, -49, 40.5, false));

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            //requestOpModeStop();
                        }
                    });

                    runQueue.submitAction(new DelayAction(300));

                    runQueue.submitAction(new Action() {
                        @Override
                        public void update() {

                        }

                        @Override
                        public boolean shouldDeactivate() {
                            return hardware.getTurretSystem().getExtensionPosition() > 500;
                        }
                    });

                    runQueue.submitAction(new DelayAction(200));

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            hardware.getTurretSystem().openArm();
                        }
                    });

                    runQueue.submitAction(new DelayAction(500));

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            hardware.getTurretSystem().closeArm();
                            hardware.getTurretSystem().setBucketPosRaw(0.4);
                        }
                    });

                    runQueue.submitAction(new DelayAction(300));

                    runQueue.submitAction(new InstantAction() {
                        @Override
                        public void update() {
                            ActionController.addAction(BlueGoalActions.getBlueAllianceReturn(hardware));
                        }
                    });


                    runQueue.submitAction(new DelayAction(100));
                }
                runQueue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        RobotLog.i("Got Here 1");
                    }
                });
                runQueue.submitAction(system.gotoGvf(new Position(3, 40, Angle.ZERO())));
                runQueue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        RobotLog.i("Got Here 2");
                    }
                });
                runQueue.submitAction(new EnterIntakeAction(hardware));
                runQueue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        RobotLog.i("Got Here 3");
                    }
                });
                runQueue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        hardware.getTurretSystem().setExPIDActive(false);
                        hardware.getTurretSystem().setTurretPIDActive(false);
                        hardware.getTurretSystem().setExtensionMotorPower(0);
                        hardware.getTurretSystem().setTurretMotorPower(0);
                    }
                });
                runQueue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        RobotLog.i("Got Here 4");
                    }
                });
                runQueue.submitAction(new DelayAction(800));
                runQueue.submitAction(new InstantAction() {
                    @Override
                    public void update() {
                        RobotLog.i("Got Here 5");
                    }
                });
                ActionController.addAction(runQueue);
            }
        });


    }

    public enum LEVEL{
        HIGH,
        MED,
        LOW
    }
}
