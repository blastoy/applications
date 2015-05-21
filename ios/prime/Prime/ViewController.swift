//
//  ViewController.swift
//  Prime
//
//  Created by Aymara Ruiz on 3/1/15.
//  Copyright (c) 2015 Aymara Ruiz. All rights reserved.
//

import UIKit

class ViewController: UIViewController {

    @IBOutlet weak var textBox: UITextField!
    @IBOutlet weak var subButton: UIButton!
    @IBOutlet weak var ansLabel: UILabel!
    
    @IBAction func subButtonPressed(sender: AnyObject)
    {
        var input = textBox.text.toInt()
        var isPrime = true
        
        if input == 1
        {
            isPrime = false
        }
            
        else
        {
            for var i = 2 ; i < input ; i++
            {
                if input! % i == 0
                {
                    isPrime = false
                    break
                }
            }
        }
        
        if isPrime
        {
            ansLabel.text = "It is a prime number!"
        }
        else
        {
            ansLabel.text = "It is not a prime number!"
        }
    }
    
    override func viewDidLoad()
    {
        super.viewDidLoad()
        // Do any additional setup after loading the view, typically from a nib.
    }

    override func didReceiveMemoryWarning()
    {
        super.didReceiveMemoryWarning()
        // Dispose of any resources that can be recreated.
    }


}

