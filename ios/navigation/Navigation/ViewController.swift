//
//  ViewController.swift
//  Navigation
//
//  Created by Aymara Ruiz on 3/1/15.
//  Copyright (c) 2015 Aymara Ruiz. All rights reserved.
//

import UIKit

class ViewController: UIViewController {
    
    @IBOutlet weak var actionButton: UIBarButtonItem!
    
    @IBOutlet weak var timerLabel: UILabel!
    
    var stopped = true
    var timer = NSTimer()
    var count = 0

    func result()
    {
        count++
        timerLabel.text = "\(count)"
    }
    
    @IBAction func pauseButtonPressed(sender: AnyObject)
    {
        if !stopped
        {
            timer.invalidate()
            stopped = true
        }
    }
    
    @IBAction func actionButtonPressed(sender: AnyObject)
    {
        if stopped
        {
            timer = NSTimer.scheduledTimerWithTimeInterval(1, target: self, selector: Selector("result"), userInfo: nil, repeats: true)
            
            stopped = false
        }
    }
    
    @IBAction func resetButtonPressed(sender: AnyObject)
    {
        timer.invalidate()
        count = 0
        stopped = true
        timerLabel.text = "\(count)"
        
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
    }

    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }
}

