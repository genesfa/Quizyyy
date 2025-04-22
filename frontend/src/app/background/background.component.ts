import {Component, OnInit} from '@angular/core';
import {NgParticlesService, NgxParticlesModule} from "@tsparticles/angular";
import {Container, MoveDirection, OutMode, tsParticles} from '@tsparticles/engine';
import {loadSlim} from '@tsparticles/slim';

@Component({
  selector: 'app-background',
    imports: [
        NgxParticlesModule
    ],
  templateUrl: './background.component.html',
  styleUrl: './background.component.css'
})
export class BackgroundComponent implements OnInit{
  particlesOptions = {
    background: {
      color: {
        value: "#000000",
      },
    },
    fpsLimit: 120,
    particles: {
      color: {
        value: "#ffffff",
      },
      links: {
        color: "#ffffff",
       // distance: 90,
        enable: true,
        opacity: 0.5,
        width: 1,
      },
      move: {
        direction: MoveDirection.none,
        enable: true,
        outModes: {
          default: OutMode.bounce,
        },
        random: false,
        speed: 0.3,
        straight: false,
      },
      number: {
        density: {
          enable: true,
        },
        value: 800,
      },
      opacity: {
        value: 0.5,
      },
      shape: {
        type: "circle",
      },
      size: {
        value: { min: 0.1, max: 0.5 },
      },
    },
    detectRetina: true,
  };




  id = "background";

  ngOnInit(): void {
    this.ngParticlesService.init(async (engine) => {
      console.log(engine);

      // Starting from 1.19.0 you can add custom presets or shape here, using the current tsParticles instance (main)
      // this loads the tsparticles package bundle, it's the easiest method for getting everything ready
      // starting from v2 you can add only the features you need reducing the bundle size

      await loadSlim(engine);

    });
  }
   constructor(private readonly ngParticlesService: NgParticlesService) {
   }

  particlesLoaded(container: Container): void {
    console.log(container);
  }


}
