import {defineConfig} from '@playwright/test';
export default defineConfig({testDir:'./tests',fullyParallel:false,workers:1,retries:0,timeout:45000,reporter:[['list'],['html',{open:'never'}],['junit',{outputFile:'test-results/junit.xml'}]],use:{baseURL:process.env.BASE_URL??'http://localhost:8080',browserName:'chromium',headless:true,trace:'retain-on-failure',screenshot:'only-on-failure'}});
