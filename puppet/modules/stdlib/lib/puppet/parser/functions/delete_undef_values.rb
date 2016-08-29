module Puppet::Parser::Functions
  newfunction(:delete_undef_values, :type => :rvalue, :doc => <<-EOS
Returns a copy of input hash or array with all undefs deleted.

*Examples:*
    
    $hash = delete_undef_values({a=>'A', b=>'', c=>undef, d => false})

Would return: {a => 'A', b => '', d => false}

    $array = delete_undef_values(['A','',undef,false])

Would return: ['A','',false]
    
      EOS
    ) do |args|

    raise(Puppet::ParseError,
          "delete_undef_values(): Wrong number of arguments given " +
          "(#{args.size})") if args.size < 1

    result = args[0]
    if result.is_a?(Hash)
      result.delete_if {|key, val| val.equal? :undef}
    elsif result.is_a?(Array)
      result.delete :undef
    else
      raise(Puppet::ParseError, 
            "delete_undef_values(): Wrong argument type #{args[0].class} " +
            "for first argument")
    end
    result
  end
end
